package com.workerpay.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DebtPaymentHistoryDTO(
    String workerName,
    String debtDescription,
    BigDecimal amount,
    LocalDate paymentDate,
    String notes,
    LocalDateTime createdAt
) {
}
