package com.workerpay.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ActiveDebtReportDTO(
    String workerName,
    String workerPosition,
    BigDecimal originalAmount,
    BigDecimal currentBalance,
    BigDecimal suggestedPayment,
    String description,
    String status,
    LocalDateTime createdAt
) {
}
