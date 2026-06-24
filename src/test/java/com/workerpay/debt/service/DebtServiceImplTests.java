package com.workerpay.debt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.workerpay.common.service.AuditService;
import com.workerpay.debt.dto.DebtForm;
import com.workerpay.debt.dto.DebtPaymentForm;
import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtStatus;
import com.workerpay.debt.repository.DebtPaymentRepository;
import com.workerpay.debt.repository.DebtRepository;
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

@ExtendWith(MockitoExtension.class)
class DebtServiceImplTests {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private DebtPaymentRepository debtPaymentRepository;

    @Mock
    private WorkerService workerService;

    @Mock
    private AuditService auditService;

    private DebtServiceImpl debtService;

    @BeforeEach
    void setUp() {
        debtService = new DebtServiceImpl(debtRepository, debtPaymentRepository, workerService, auditService);
    }

    @Test
    void createDebtStartsActiveWithFullBalance() {
        when(workerService.findById(1L)).thenReturn(worker());
        when(debtRepository.save(any(Debt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Debt debt = debtService.create(form());

        assertThat(debt.getStatus()).isEqualTo(DebtStatus.ACTIVE);
        assertThat(debt.getOriginalAmount()).isEqualByComparingTo("300.00");
        assertThat(debt.getCurrentBalance()).isEqualByComparingTo("300.00");
    }

    @Test
    void paymentReducesBalance() {
        Debt debt = debt();
        when(debtRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(debt));
        when(debtPaymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        debtService.addPayment(10L, paymentForm("125.00"));

        assertThat(debt.getCurrentBalance()).isEqualByComparingTo("175.00");
        assertThat(debt.getStatus()).isEqualTo(DebtStatus.ACTIVE);
    }

    @Test
    void paymentMarksDebtAsPaidWhenBalanceReachesZero() {
        Debt debt = debt();
        when(debtRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(debt));
        when(debtPaymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        debtService.addPayment(10L, paymentForm("300.00"));

        assertThat(debt.getCurrentBalance()).isEqualByComparingTo("0.00");
        assertThat(debt.getStatus()).isEqualTo(DebtStatus.PAID);
    }

    @Test
    void doesNotAllowPaymentGreaterThanBalance() {
        when(debtRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(debt()));

        assertThatThrownBy(() -> debtService.addPayment(10L, paymentForm("301.00")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("mayor al saldo");
    }

    @Test
    void doesNotAllowPaymentOnCancelledDebt() {
        Debt debt = debt();
        debt.setStatus(DebtStatus.CANCELLED);
        when(debtRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(debt));

        assertThatThrownBy(() -> debtService.addPayment(10L, paymentForm("50.00")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("activas");
    }

    private DebtForm form() {
        DebtForm form = new DebtForm();
        form.setWorkerId(1L);
        form.setOriginalAmount(new BigDecimal("300.00"));
        form.setSuggestedPayment(new BigDecimal("75.00"));
        form.setDescription("Prestamo");
        return form;
    }

    private DebtPaymentForm paymentForm(String amount) {
        DebtPaymentForm form = new DebtPaymentForm();
        form.setAmount(new BigDecimal(amount));
        form.setPaymentDate(LocalDate.of(2026, 6, 22));
        form.setNotes("Abono");
        return form;
    }

    private Debt debt() {
        Debt debt = new Debt();
        debt.setWorker(worker());
        debt.setOriginalAmount(new BigDecimal("300.00"));
        debt.setCurrentBalance(new BigDecimal("300.00"));
        debt.setSuggestedPayment(new BigDecimal("75.00"));
        debt.setStatus(DebtStatus.ACTIVE);
        return debt;
    }

    private Worker worker() {
        Worker worker = new Worker();
        worker.setFullName("Ana Lopez");
        worker.setPosition("Operaciones");
        worker.setPaymentType(PaymentType.WEEKLY);
        worker.setBaseSalary(new BigDecimal("500.00"));
        worker.setHireDate(LocalDate.of(2026, 1, 1));
        worker.setActive(true);
        return worker;
    }
}
