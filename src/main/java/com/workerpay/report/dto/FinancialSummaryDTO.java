package com.workerpay.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialSummaryDTO(
    long activeWorkersCount,
    long pendingAdvancesCount,
    BigDecimal pendingAdvancesTotal,
    long activeDebtsCount,
    BigDecimal activeDebtsTotal,
    long pendingPaymentsCount,
    BigDecimal paidPaymentsTotal,
    BigDecimal netPaidTotal,
    BigDecimal totalDiscounts,
    List<PayrollByPeriodReportDTO> recentPayments,
    List<PendingAdvanceReportDTO> recentAdvances,
    List<ActiveDebtReportDTO> recentDebts
) {
}
