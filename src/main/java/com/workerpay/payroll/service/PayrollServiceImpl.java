package com.workerpay.payroll.service;

import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import com.workerpay.advance.repository.AdvanceRepository;
import com.workerpay.common.exception.ResourceNotFoundException;
import com.workerpay.common.service.AuditService;
import com.workerpay.common.util.MoneyUtils;
import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtPayment;
import com.workerpay.debt.entity.DebtStatus;
import com.workerpay.debt.repository.DebtPaymentRepository;
import com.workerpay.debt.repository.DebtRepository;
import com.workerpay.payroll.dto.PayrollPaymentForm;
import com.workerpay.payroll.entity.PaymentPeriod;
import com.workerpay.payroll.entity.PaymentPeriodStatus;
import com.workerpay.payroll.entity.PayrollPayment;
import com.workerpay.payroll.entity.PayrollPaymentStatus;
import com.workerpay.payroll.repository.PaymentPeriodRepository;
import com.workerpay.payroll.repository.PayrollPaymentRepository;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.service.WorkerService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollPaymentRepository payrollPaymentRepository;
    private final PaymentPeriodRepository paymentPeriodRepository;
    private final WorkerService workerService;
    private final AdvanceRepository advanceRepository;
    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final AuditService auditService;

    public PayrollServiceImpl(
        PayrollPaymentRepository payrollPaymentRepository,
        PaymentPeriodRepository paymentPeriodRepository,
        WorkerService workerService,
        AdvanceRepository advanceRepository,
        DebtRepository debtRepository,
        DebtPaymentRepository debtPaymentRepository,
        AuditService auditService
    ) {
        this.payrollPaymentRepository = payrollPaymentRepository;
        this.paymentPeriodRepository = paymentPeriodRepository;
        this.workerService = workerService;
        this.advanceRepository = advanceRepository;
        this.debtRepository = debtRepository;
        this.debtPaymentRepository = debtPaymentRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPayment> findAllPayments() {
        return payrollPaymentRepository.findAllByOrderByPaymentDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollPayment findPaymentById(Long id) {
        return payrollPaymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
    }

    @Override
    public PayrollPayment createPayment(PayrollPaymentForm form) {
        Worker worker = workerService.findById(form.getWorkerId());
        PaymentPeriod period = findPeriod(form.getPeriodId());
        requireOpenPeriod(period);
        if (payrollPaymentRepository.existsByWorkerIdAndPeriodIdAndStatusNot(
            form.getWorkerId(),
            form.getPeriodId(),
            PayrollPaymentStatus.CANCELLED
        )) {
            throw new IllegalStateException("Ya existe un pago activo para este trabajador en el periodo seleccionado.");
        }
        PayrollPayment payment = new PayrollPayment();
        payment.setWorker(worker);
        payment.setPeriod(period);
        applyForm(payment, form);
        payment.setStatus(PayrollPaymentStatus.PENDING);
        PayrollPayment saved = payrollPaymentRepository.save(payment);
        auditService.logChange("CREATE", "PayrollPayment", saved.getId(), saved.getNetPayment().toPlainString());
        return saved;
    }

    @Override
    @Transactional(timeout = 15)
    public PayrollPayment updatePayment(Long id, PayrollPaymentForm form) {
        PayrollPayment payment = payrollPaymentRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
        requirePending(payment, "Solo se pueden editar pagos pendientes.");
        PaymentPeriod period = findPeriod(form.getPeriodId());
        requireOpenPeriod(period);
        boolean duplicate = payrollPaymentRepository.existsByWorkerIdAndPeriodIdAndStatusNot(
            form.getWorkerId(),
            form.getPeriodId(),
            PayrollPaymentStatus.CANCELLED
        );
        boolean samePayment = payment.getWorker().getId().equals(form.getWorkerId())
            && payment.getPeriod().getId().equals(form.getPeriodId());
        if (duplicate && !samePayment) {
            throw new IllegalStateException("Ya existe un pago activo para este trabajador en el periodo seleccionado.");
        }
        payment.setWorker(workerService.findById(form.getWorkerId()));
        payment.setPeriod(period);
        applyForm(payment, form);
        PayrollPayment saved = payrollPaymentRepository.save(payment);
        auditService.logChange("UPDATE", "PayrollPayment", saved.getId(), saved.getNetPayment().toPlainString());
        return saved;
    }

    @Override
    public void cancelPayment(Long id) {
        PayrollPayment payment = findPaymentById(id);
        if (payment.getStatus() == PayrollPaymentStatus.PAID) {
            throw new IllegalStateException("No se puede cancelar un pago ya pagado.");
        }
        if (payment.getStatus() == PayrollPaymentStatus.CANCELLED) {
            return;
        }
        payment.setStatus(PayrollPaymentStatus.CANCELLED);
        payrollPaymentRepository.save(payment);
        auditService.logChange("CANCEL", "PayrollPayment", payment.getId(), payment.getNetPayment().toPlainString());
    }

    @Override
    @Transactional(timeout = 15)
    public boolean markAsPaid(Long id) {
        PayrollPayment payment = payrollPaymentRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
        requirePending(payment, "Solo se pueden marcar como pagados los pagos pendientes.");
        boolean advancesMatched = markAdvancesIfExact(payment);
        applyDebtDiscount(payment);
        payment.setStatus(PayrollPaymentStatus.PAID);
        payrollPaymentRepository.save(payment);
        auditService.logChange("PAY", "PayrollPayment", payment.getId(), payment.getNetPayment().toPlainString());
        return advancesMatched;
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingPayments() {
        return payrollPaymentRepository.countByStatus(PayrollPaymentStatus.PENDING);
    }

    @Override
    public BigDecimal calculateNetPayment(
        BigDecimal baseAmount,
        BigDecimal bonuses,
        BigDecimal advanceDiscount,
        BigDecimal debtDiscount,
        BigDecimal otherDiscounts
    ) {
        BigDecimal netPayment = MoneyUtils.normalize(baseAmount)
            .add(MoneyUtils.normalize(bonuses))
            .subtract(MoneyUtils.normalize(advanceDiscount))
            .subtract(MoneyUtils.normalize(debtDiscount))
            .subtract(MoneyUtils.normalize(otherDiscounts));
        if (netPayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El pago neto no puede ser negativo. Ajusta descuentos o bonos.");
        }
        return MoneyUtils.normalize(netPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSuggestedAdvanceDiscount(Long workerId) {
        return advanceRepository.findByWorkerIdAndStatus(workerId, AdvanceStatus.PENDING)
            .stream()
            .map(Advance::getAmount)
            .map(MoneyUtils::normalize)
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPaidTotalForCurrentOpenPeriod() {
        return paymentPeriodRepository.findFirstByStatusOrderByStartDateDesc(PaymentPeriodStatus.OPEN)
            .map(period -> MoneyUtils.normalize(payrollPaymentRepository.sumNetPaymentByPeriodIdAndStatus(
                period.getId(),
                PayrollPaymentStatus.PAID
            )))
            .orElse(BigDecimal.ZERO.setScale(2));
    }

    @Override
    public PayrollPaymentForm toForm(PayrollPayment payment) {
        PayrollPaymentForm form = new PayrollPaymentForm();
        form.setWorkerId(payment.getWorker().getId());
        form.setPeriodId(payment.getPeriod().getId());
        form.setBaseAmount(payment.getBaseAmount());
        form.setBonuses(payment.getBonuses());
        form.setAdvanceDiscount(payment.getAdvanceDiscount());
        form.setDebtDiscount(payment.getDebtDiscount());
        form.setOtherDiscounts(payment.getOtherDiscounts());
        form.setPaymentDate(payment.getPaymentDate());
        return form;
    }

    private void applyForm(PayrollPayment payment, PayrollPaymentForm form) {
        payment.setBaseAmount(MoneyUtils.normalize(form.getBaseAmount()));
        payment.setBonuses(MoneyUtils.normalize(form.getBonuses()));
        payment.setAdvanceDiscount(MoneyUtils.normalize(form.getAdvanceDiscount()));
        payment.setDebtDiscount(MoneyUtils.normalize(form.getDebtDiscount()));
        payment.setOtherDiscounts(MoneyUtils.normalize(form.getOtherDiscounts()));
        payment.setPaymentDate(form.getPaymentDate());
        payment.setNetPayment(calculateNetPayment(
            payment.getBaseAmount(),
            payment.getBonuses(),
            payment.getAdvanceDiscount(),
            payment.getDebtDiscount(),
            payment.getOtherDiscounts()
        ));
    }

    private PaymentPeriod findPeriod(Long id) {
        return paymentPeriodRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Periodo de pago no encontrado"));
    }

    private void requireOpenPeriod(PaymentPeriod period) {
        if (period.getStatus() != PaymentPeriodStatus.OPEN) {
            throw new IllegalStateException("El periodo seleccionado esta cerrado.");
        }
    }

    private void requirePending(PayrollPayment payment, String message) {
        if (payment.getStatus() != PayrollPaymentStatus.PENDING) {
            throw new IllegalStateException(message);
        }
    }

    private boolean markAdvancesIfExact(PayrollPayment payment) {
        BigDecimal discount = MoneyUtils.normalize(payment.getAdvanceDiscount());
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        List<Advance> pendingAdvances = advanceRepository.findByWorkerIdAndStatusForUpdate(
            payment.getWorker().getId(),
            AdvanceStatus.PENDING
        );
        BigDecimal pendingTotal = pendingAdvances.stream()
            .map(Advance::getAmount)
            .map(MoneyUtils::normalize)
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        if (pendingTotal.compareTo(discount) != 0) {
            return false;
        }
        pendingAdvances.forEach(advance -> advance.setStatus(AdvanceStatus.DISCOUNTED));
        advanceRepository.saveAll(pendingAdvances);
        return true;
    }

    private void applyDebtDiscount(PayrollPayment payment) {
        BigDecimal remainingDiscount = MoneyUtils.normalize(payment.getDebtDiscount());
        if (remainingDiscount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        List<Debt> activeDebts = debtRepository.findByWorkerIdAndStatusForUpdate(
            payment.getWorker().getId(),
            DebtStatus.ACTIVE
        );
        BigDecimal activeBalance = activeDebts.stream()
            .map(Debt::getCurrentBalance)
            .map(MoneyUtils::normalize)
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        if (remainingDiscount.compareTo(activeBalance) > 0) {
            throw new IllegalStateException("El descuento por deuda supera el saldo activo del trabajador.");
        }

        List<DebtPayment> payments = new ArrayList<>();
        for (Debt debt : activeDebts) {
            if (remainingDiscount.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
            BigDecimal debtBalance = MoneyUtils.normalize(debt.getCurrentBalance());
            BigDecimal appliedAmount = remainingDiscount.min(debtBalance);
            DebtPayment debtPayment = new DebtPayment();
            debtPayment.setDebt(debt);
            debtPayment.setAmount(appliedAmount);
            debtPayment.setPaymentDate(payment.getPaymentDate());
            debtPayment.setNotes("Descuento aplicado desde pago de nomina.");
            payments.add(debtPayment);

            BigDecimal newBalance = debtBalance.subtract(appliedAmount);
            debt.setCurrentBalance(MoneyUtils.normalize(newBalance));
            if (debt.getCurrentBalance().compareTo(BigDecimal.ZERO) == 0) {
                debt.setStatus(DebtStatus.PAID);
            }
            remainingDiscount = remainingDiscount.subtract(appliedAmount);
        }
        debtRepository.saveAll(activeDebts);
        debtPaymentRepository.saveAll(payments);
    }
}
