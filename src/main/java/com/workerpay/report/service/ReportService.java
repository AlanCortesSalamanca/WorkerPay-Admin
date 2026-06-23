package com.workerpay.report.service;

import com.workerpay.report.dto.ActiveDebtReportDTO;
import com.workerpay.report.dto.FinancialSummaryDTO;
import com.workerpay.report.dto.PayrollByPeriodReportDTO;
import com.workerpay.report.dto.PendingAdvanceReportDTO;
import com.workerpay.report.dto.ReportFilterForm;
import com.workerpay.report.dto.ReportTotalsDTO;
import com.workerpay.report.dto.WorkerFinancialHistoryDTO;
import java.util.List;

public interface ReportService {

    List<PayrollByPeriodReportDTO> getPayrollByPeriodReport(ReportFilterForm filter);

    ReportTotalsDTO getPayrollByPeriodTotals(ReportFilterForm filter);

    List<PendingAdvanceReportDTO> getPendingAdvancesReport(ReportFilterForm filter);

    ReportTotalsDTO getPendingAdvancesTotals(ReportFilterForm filter);

    List<ActiveDebtReportDTO> getActiveDebtsReport(ReportFilterForm filter);

    ReportTotalsDTO getActiveDebtsTotals(ReportFilterForm filter);

    WorkerFinancialHistoryDTO getWorkerFinancialHistory(Long workerId);

    FinancialSummaryDTO getFinancialSummary();

    String exportPayrollByPeriodCsv(ReportFilterForm filter);

    String exportPendingAdvancesCsv(ReportFilterForm filter);

    String exportActiveDebtsCsv(ReportFilterForm filter);

    String exportWorkerHistoryCsv(Long workerId);

    String exportFinancialSummaryCsv();
}
