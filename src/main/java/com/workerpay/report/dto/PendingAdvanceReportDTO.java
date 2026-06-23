package com.workerpay.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PendingAdvanceReportDTO(
    String workerName,
    String workerPosition,
    BigDecimal amount,
    LocalDate date,
    String reason,
    String status,
    LocalDateTime createdAt
) {
}
