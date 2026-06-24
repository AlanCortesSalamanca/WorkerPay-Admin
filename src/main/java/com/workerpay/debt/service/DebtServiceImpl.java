package com.workerpay.debt.service;

import com.workerpay.common.exception.ResourceNotFoundException;
import com.workerpay.common.service.AuditService;
import com.workerpay.common.util.MoneyUtils;
import com.workerpay.debt.dto.DebtForm;
import com.workerpay.debt.dto.DebtPaymentForm;
import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtPayment;
import com.workerpay.debt.entity.DebtStatus;
import com.workerpay.debt.repository.DebtPaymentRepository;
import com.workerpay.debt.repository.DebtRepository;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.service.WorkerService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final WorkerService workerService;
    private final AuditService auditService;

    public DebtServiceImpl(
        DebtRepository debtRepository,
        DebtPaymentRepository debtPaymentRepository,
        WorkerService workerService,
        AuditService auditService
    ) {
        this.debtRepository = debtRepository;
        this.debtPaymentRepository = debtPaymentRepository;
        this.workerService = workerService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Debt> findAll() {
        return debtRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Debt findById(Long id) {
        return debtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada"));
    }

    @Override
    public Debt create(DebtForm form) {
        Debt debt = new Debt();
        Worker worker = workerService.findById(form.getWorkerId());
        BigDecimal originalAmount = MoneyUtils.normalize(form.getOriginalAmount());
        debt.setWorker(worker);
        debt.setOriginalAmount(originalAmount);
        debt.setCurrentBalance(originalAmount);
        debt.setSuggestedPayment(MoneyUtils.normalize(form.getSuggestedPayment()));
        debt.setDescription(clean(form.getDescription()));
        debt.setStatus(DebtStatus.ACTIVE);
        Debt saved = debtRepository.save(debt);
        auditService.logChange("CREATE", "Debt", saved.getId(), saved.getOriginalAmount().toPlainString());
        return saved;
    }

    @Override
    public Debt update(Long id, DebtForm form) {
        Debt debt = findById(id);
        requireActive(debt, "Solo se pueden editar deudas activas.");
        debt.setWorker(workerService.findById(form.getWorkerId()));
        debt.setSuggestedPayment(MoneyUtils.normalize(form.getSuggestedPayment()));
        debt.setDescription(clean(form.getDescription()));
        Debt saved = debtRepository.save(debt);
        auditService.logChange("UPDATE", "Debt", saved.getId(), saved.getCurrentBalance().toPlainString());
        return saved;
    }

    @Override
    public void cancel(Long id) {
        Debt debt = findById(id);
        if (debt.getStatus() == DebtStatus.PAID) {
            throw new IllegalStateException("No se puede cancelar una deuda liquidada.");
        }
        if (debt.getStatus() == DebtStatus.CANCELLED) {
            return;
        }
        debt.setStatus(DebtStatus.CANCELLED);
        debtRepository.save(debt);
        auditService.logChange("CANCEL", "Debt", debt.getId(), debt.getCurrentBalance().toPlainString());
    }

    @Override
    @Transactional(timeout = 15)
    public DebtPayment addPayment(Long debtId, DebtPaymentForm form) {
        Debt debt = debtRepository.findByIdForUpdate(debtId)
            .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada"));
        requireActive(debt, "Solo se pueden registrar abonos en deudas activas.");
        BigDecimal amount = MoneyUtils.normalize(form.getAmount());
        if (amount.compareTo(debt.getCurrentBalance()) > 0) {
            throw new IllegalStateException("El abono no puede ser mayor al saldo actual.");
        }
        DebtPayment payment = new DebtPayment();
        payment.setDebt(debt);
        payment.setAmount(amount);
        payment.setPaymentDate(form.getPaymentDate());
        payment.setNotes(clean(form.getNotes()));
        debt.setCurrentBalance(debt.getCurrentBalance().subtract(amount));
        if (debt.getCurrentBalance().compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(DebtStatus.PAID);
        }
        debtRepository.save(debt);
        DebtPayment savedPayment = debtPaymentRepository.save(payment);
        auditService.logChange("PAYMENT", "Debt", debt.getId(), amount.toPlainString());
        return savedPayment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtPayment> findPayments(Long debtId) {
        return debtPaymentRepository.findByDebtIdOrderByPaymentDateDesc(debtId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Debt> findActive() {
        return debtRepository.findByStatus(DebtStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Debt> findActiveByWorkerId(Long workerId) {
        return debtRepository.findByWorkerIdAndStatus(workerId, DebtStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return debtRepository.countByStatus(DebtStatus.ACTIVE);
    }

    @Override
    public DebtForm toForm(Debt debt) {
        DebtForm form = new DebtForm();
        form.setWorkerId(debt.getWorker().getId());
        form.setOriginalAmount(debt.getOriginalAmount());
        form.setSuggestedPayment(debt.getSuggestedPayment());
        form.setDescription(debt.getDescription());
        return form;
    }

    private void requireActive(Debt debt, String message) {
        if (debt.getStatus() != DebtStatus.ACTIVE) {
            throw new IllegalStateException(message);
        }
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
