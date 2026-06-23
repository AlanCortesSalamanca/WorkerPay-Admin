package com.workerpay.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkerFinancialHistoryDTO(
    Long workerId,
    String workerName,
    String workerPosition,
    String paymentType,
    BigDecimal baseSalary,
    boolean active,
    List<PayrollByPeriodReportDTO> payments,
    List<PendingAdvanceReportDTO> advances,
    List<ActiveDebtReportDTO> debts,
    List<DebtPaymentHistoryDTO> debtPayments,
    BigDecimal totalPaid,
    BigDecimal totalPendingAdvances,
    BigDecimal totalDiscountedAdvances,
    long activeDebtsCount,
    BigDecimal totalPendingDebtBalance,
    BigDecimal totalDebtPayments
) {
}
