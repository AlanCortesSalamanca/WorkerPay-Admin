package com.workerpay.advance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workerpay.advance.dto.AdvanceForm;
import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import com.workerpay.advance.repository.AdvanceRepository;
import com.workerpay.common.service.AuditService;
import com.workerpay.worker.entity.PaymentType;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.service.WorkerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdvanceServiceImplTests {

    @Mock
    private AdvanceRepository advanceRepository;

    @Mock
    private WorkerService workerService;

    @Mock
    private AuditService auditService;

    private AdvanceServiceImpl advanceService;

    @BeforeEach
    void setUp() {
        advanceService = new AdvanceServiceImpl(advanceRepository, workerService, auditService);
    }

    @Test
    void createStoresPendingAdvance() {
        Worker worker = worker();
        AdvanceForm form = form();
        when(workerService.findById(1L)).thenReturn(worker);
        when(advanceRepository.save(any(Advance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Advance created = advanceService.create(form);

        assertThat(created.getStatus()).isEqualTo(AdvanceStatus.PENDING);
        assertThat(created.getWorker()).isEqualTo(worker);
        assertThat(created.getAmount()).isEqualByComparingTo("150.25");
    }

    @Test
    void updateDoesNotAllowCancelledAdvance() {
        Advance cancelled = advance();
        cancelled.setStatus(AdvanceStatus.CANCELLED);
        when(advanceRepository.findById(10L)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> advanceService.update(10L, form()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("pendientes");
    }

    @Test
    void markAsDiscountedDoesNotAllowCancelledAdvance() {
        Advance cancelled = advance();
        cancelled.setStatus(AdvanceStatus.CANCELLED);
        when(advanceRepository.findById(10L)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> advanceService.markAsDiscounted(10L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("cancelado");
    }

    @Test
    void countPendingUsesPendingStatus() {
        when(advanceRepository.countByStatus(AdvanceStatus.PENDING)).thenReturn(3L);

        long count = advanceService.countPending();

        assertThat(count).isEqualTo(3L);
        ArgumentCaptor<AdvanceStatus> captor = ArgumentCaptor.forClass(AdvanceStatus.class);
        verify(advanceRepository).countByStatus(captor.capture());
        assertThat(captor.getValue()).isEqualTo(AdvanceStatus.PENDING);
    }

    private AdvanceForm form() {
        AdvanceForm form = new AdvanceForm();
        form.setWorkerId(1L);
        form.setAmount(new BigDecimal("150.25"));
        form.setDate(LocalDate.of(2026, 6, 22));
        form.setReason("Apoyo de transporte");
        return form;
    }

    private Advance advance() {
        Advance advance = new Advance();
        advance.setWorker(worker());
        advance.setAmount(new BigDecimal("100.00"));
        advance.setDate(LocalDate.of(2026, 6, 22));
        advance.setReason("Motivo");
        advance.setStatus(AdvanceStatus.PENDING);
        return advance;
    }

    private Worker worker() {
        Worker worker = new Worker();
        worker.setFullName("Juan Rodriguez");
        worker.setPosition("Operaciones");
        worker.setPaymentType(PaymentType.WEEKLY);
        worker.setBaseSalary(new BigDecimal("500.00"));
        worker.setHireDate(LocalDate.of(2026, 1, 1));
        worker.setActive(true);
        return worker;
    }
}
