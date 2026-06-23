package com.workerpay.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollByPeriodReportDTO(
    String periodName,
    String workerName,
    BigDecimal baseAmount,
    BigDecimal bonuses,
    BigDecimal advanceDiscount,
    BigDecimal debtDiscount,
    BigDecimal otherDiscounts,
    BigDecimal netPayment,
    String status,
    LocalDate paymentDate
) {
}
