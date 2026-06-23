package com.workerpay.report.dto;

import java.math.BigDecimal;

public record ReportTotalsDTO(
    long count,
    BigDecimal totalBaseAmount,
    BigDecimal totalBonuses,
    BigDecimal totalAdvanceDiscount,
    BigDecimal totalDebtDiscount,
    BigDecimal totalOtherDiscounts,
    BigDecimal totalNetPayment,
    BigDecimal totalAmount,
    BigDecimal totalBalance
) {
}
