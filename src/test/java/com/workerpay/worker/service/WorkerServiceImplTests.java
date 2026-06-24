package com.workerpay.worker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workerpay.common.service.AuditService;
import com.workerpay.worker.dto.WorkerForm;
import com.workerpay.worker.entity.PaymentType;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.repository.WorkerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkerServiceImplTests {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private AuditService auditService;

    private WorkerServiceImpl workerService;

    @BeforeEach
    void setUp() {
        workerService = new WorkerServiceImpl(workerRepository, auditService);
    }

    @Test
    void createNormalizesSalaryAndTrimsRequiredText() {
        when(workerRepository.save(any(Worker.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Worker worker = workerService.create(form());

        assertThat(worker.getFullName()).isEqualTo("Ana Lopez");
        assertThat(worker.getPosition()).isEqualTo("Produccion");
        assertThat(worker.getBaseSalary()).isEqualByComparingTo("500.10");
        verify(auditService).logChange("CREATE", "Worker", worker.getId(), worker.getFullName());
    }

    @Test
    void findActiveUsesActiveRepositoryQuery() {
        Worker worker = worker();
        when(workerRepository.findByActiveTrueOrderByFullNameAsc()).thenReturn(List.of(worker));

        assertThat(workerService.findActive()).containsExactly(worker);
    }

    @Test
    void deactivateMarksWorkerInactiveAndAudits() {
        Worker worker = worker();
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));

        workerService.deactivate(1L);

        assertThat(worker.isActive()).isFalse();
        verify(auditService).logChange("DEACTIVATE", "Worker", worker.getId(), worker.getFullName());
    }

    private WorkerForm form() {
        WorkerForm form = new WorkerForm();
        form.setFullName(" Ana Lopez ");
        form.setPhone("555-0101");
        form.setPosition(" Produccion ");
        form.setPaymentType(PaymentType.WEEKLY);
        form.setBaseSalary(new BigDecimal("500.1"));
        form.setHireDate(LocalDate.of(2026, 1, 1));
        form.setActive(true);
        return form;
    }

    private Worker worker() {
        Worker worker = new Worker();
        worker.setFullName("Ana Lopez");
        worker.setPosition("Produccion");
        worker.setPaymentType(PaymentType.WEEKLY);
        worker.setBaseSalary(new BigDecimal("500.00"));
        worker.setHireDate(LocalDate.of(2026, 1, 1));
        worker.setActive(true);
        return worker;
    }
}
