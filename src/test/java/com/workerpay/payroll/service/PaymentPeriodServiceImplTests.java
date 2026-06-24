package com.workerpay.payroll.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workerpay.common.service.AuditService;
import com.workerpay.payroll.dto.PaymentPeriodForm;
import com.workerpay.payroll.entity.PaymentPeriod;
import com.workerpay.payroll.entity.PaymentPeriodStatus;
import com.workerpay.payroll.repository.PaymentPeriodRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentPeriodServiceImplTests {

    @Mock
    private PaymentPeriodRepository paymentPeriodRepository;

    @Mock
    private AuditService auditService;

    private PaymentPeriodServiceImpl paymentPeriodService;

    @BeforeEach
    void setUp() {
        paymentPeriodService = new PaymentPeriodServiceImpl(paymentPeriodRepository, auditService);
    }

    @Test
    void createPeriodStartsOpenAndAudits() {
        when(paymentPeriodRepository.save(any(PaymentPeriod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentPeriod period = paymentPeriodService.createPeriod(form());

        assertThat(period.getStatus()).isEqualTo(PaymentPeriodStatus.OPEN);
        assertThat(period.getName()).isEqualTo("Junio 2026");
        verify(auditService).logChange("CREATE", "PaymentPeriod", period.getId(), period.getName());
    }

    @Test
    void createPeriodRejectsEndDateBeforeStartDate() {
        PaymentPeriodForm form = form();
        form.setEndDate(LocalDate.of(2026, 5, 31));

        assertThatThrownBy(() -> paymentPeriodService.createPeriod(form))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("fecha final");
    }

    @Test
    void closePeriodMarksClosedAndAudits() {
        PaymentPeriod period = period(PaymentPeriodStatus.OPEN);
        when(paymentPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        paymentPeriodService.closePeriod(1L);

        assertThat(period.getStatus()).isEqualTo(PaymentPeriodStatus.CLOSED);
        verify(auditService).logChange("CLOSE", "PaymentPeriod", period.getId(), period.getName());
    }

    private PaymentPeriodForm form() {
        PaymentPeriodForm form = new PaymentPeriodForm();
        form.setName(" Junio 2026 ");
        form.setStartDate(LocalDate.of(2026, 6, 1));
        form.setEndDate(LocalDate.of(2026, 6, 15));
        return form;
    }

    private PaymentPeriod period(PaymentPeriodStatus status) {
        PaymentPeriod period = new PaymentPeriod();
        period.setName("Junio 2026");
        period.setStartDate(LocalDate.of(2026, 6, 1));
        period.setEndDate(LocalDate.of(2026, 6, 15));
        period.setStatus(status);
        return period;
    }
}
