package com.workerpay.payroll.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workerpay.advance.repository.AdvanceRepository;
import com.workerpay.common.service.AuditService;
import com.workerpay.debt.entity.Debt;
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
import com.workerpay.worker.entity.PaymentType;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.service.WorkerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTests {

    @Mock
    private PayrollPaymentRepository payrollPaymentRepository;

    @Mock
    private PaymentPeriodRepository paymentPeriodRepository;

    @Mock
    private WorkerService workerService;

    @Mock
    private AdvanceRepository advanceRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private DebtPaymentRepository debtPaymentRepository;

    @Mock
    private AuditService auditService;

    private PayrollServiceImpl payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollServiceImpl(
            payrollPaymentRepository,
            paymentPeriodRepository,
            workerService,
            advanceRepository,
            debtRepository,
            debtPaymentRepository,
            auditService
        );
    }

    @Test
    void calculatesNetPayment() {
        BigDecimal net = payrollService.calculateNetPayment(
            new BigDecimal("1000.00"),
            new BigDecimal("100.00"),
            new BigDecimal("50.00"),
            new BigDecimal("75.00"),
            new BigDecimal("25.00")
        );

        assertThat(net).isEqualByComparingTo("950.00");
    }

    @Test
    void doesNotAllowNegativeNetPayment() {
        assertThatThrownBy(() -> payrollService.calculateNetPayment(
            new BigDecimal("100.00"),
            BigDecimal.ZERO,
            new BigDecimal("150.00"),
            BigDecimal.ZERO,
            BigDecimal.ZERO
        )).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("negativo");
    }

    @Test
    void createPaymentStartsPending() {
        Worker worker = worker(1L);
        PaymentPeriod period = period(2L, PaymentPeriodStatus.OPEN);
        when(workerService.findById(1L)).thenReturn(worker);
        when(paymentPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(payrollPaymentRepository.existsByWorkerIdAndPeriodIdAndStatusNot(1L, 2L, PayrollPaymentStatus.CANCELLED)).thenReturn(false);
        when(payrollPaymentRepository.save(any(PayrollPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollPayment payment = payrollService.createPayment(form());

        assertThat(payment.getStatus()).isEqualTo(PayrollPaymentStatus.PENDING);
        assertThat(payment.getNetPayment()).isEqualByComparingTo("900.00");
    }

    @Test
    void doesNotAllowEditingPaidPayment() {
        PayrollPayment payment = payment(PayrollPaymentStatus.PAID);
        when(payrollPaymentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> payrollService.updatePayment(5L, form()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("pendientes");
    }

    @Test
    void doesNotAllowDuplicateActivePaymentForSameWorkerAndPeriod() {
        when(workerService.findById(1L)).thenReturn(worker(1L));
        when(paymentPeriodRepository.findById(2L)).thenReturn(Optional.of(period(2L, PaymentPeriodStatus.OPEN)));
        when(payrollPaymentRepository.existsByWorkerIdAndPeriodIdAndStatusNot(1L, 2L, PayrollPaymentStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> payrollService.createPayment(form()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Ya existe");
    }

    @Test
    void markAsPaidAppliesDebtDiscountToActiveDebt() {
        PayrollPayment payment = payment(PayrollPaymentStatus.PENDING);
        Debt debt = debt("100.00");
        when(payrollPaymentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(payment));
        when(debtRepository.findByWorkerIdAndStatusForUpdate(1L, DebtStatus.ACTIVE)).thenReturn(java.util.List.of(debt));

        payrollService.markAsPaid(5L);

        assertThat(debt.getCurrentBalance()).isEqualByComparingTo("75.00");
        assertThat(debt.getStatus()).isEqualTo(DebtStatus.ACTIVE);
        assertThat(payment.getStatus()).isEqualTo(PayrollPaymentStatus.PAID);
        verify(debtPaymentRepository).saveAll(any());
    }

    private PayrollPaymentForm form() {
        PayrollPaymentForm form = new PayrollPaymentForm();
        form.setWorkerId(1L);
        form.setPeriodId(2L);
        form.setBaseAmount(new BigDecimal("1000.00"));
        form.setBonuses(new BigDecimal("50.00"));
        form.setAdvanceDiscount(new BigDecimal("100.00"));
        form.setDebtDiscount(new BigDecimal("25.00"));
        form.setOtherDiscounts(new BigDecimal("25.00"));
        form.setPaymentDate(LocalDate.of(2026, 6, 22));
        return form;
    }

    private PayrollPayment payment(PayrollPaymentStatus status) {
        PayrollPayment payment = new PayrollPayment();
        payment.setWorker(worker(1L));
        payment.setPeriod(period(2L, PaymentPeriodStatus.OPEN));
        payment.setStatus(status);
        payment.setBaseAmount(new BigDecimal("1000.00"));
        payment.setBonuses(new BigDecimal("50.00"));
        payment.setAdvanceDiscount(new BigDecimal("0.00"));
        payment.setDebtDiscount(new BigDecimal("25.00"));
        payment.setOtherDiscounts(new BigDecimal("25.00"));
        payment.setNetPayment(new BigDecimal("1000.00"));
        payment.setPaymentDate(LocalDate.of(2026, 6, 22));
        return payment;
    }

    private Debt debt(String balance) {
        Debt debt = new Debt();
        debt.setWorker(worker(1L));
        debt.setOriginalAmount(new BigDecimal("100.00"));
        debt.setCurrentBalance(new BigDecimal(balance));
        debt.setSuggestedPayment(new BigDecimal("25.00"));
        debt.setDescription("Prestamo");
        debt.setStatus(DebtStatus.ACTIVE);
        ReflectionTestUtils.setField(debt, "createdAt", java.time.LocalDateTime.of(2026, 6, 1, 10, 0));
        ReflectionTestUtils.setField(debt, "updatedAt", java.time.LocalDateTime.of(2026, 6, 1, 10, 0));
        return debt;
    }

    private Worker worker(Long id) {
        Worker worker = new Worker();
        ReflectionTestUtils.setField(worker, "id", id);
        worker.setFullName("Luis Perez");
        worker.setPosition("Produccion");
        worker.setPaymentType(PaymentType.WEEKLY);
        worker.setBaseSalary(new BigDecimal("1000.00"));
        worker.setHireDate(LocalDate.of(2026, 1, 1));
        worker.setActive(true);
        return worker;
    }

    private PaymentPeriod period(Long id, PaymentPeriodStatus status) {
        PaymentPeriod period = new PaymentPeriod();
        ReflectionTestUtils.setField(period, "id", id);
        period.setName("Junio 2026");
        period.setStartDate(LocalDate.of(2026, 6, 1));
        period.setEndDate(LocalDate.of(2026, 6, 15));
        period.setStatus(status);
        return period;
    }
}
