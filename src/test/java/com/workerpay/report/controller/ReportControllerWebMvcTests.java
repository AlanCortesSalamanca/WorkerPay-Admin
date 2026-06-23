package com.workerpay.report.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workerpay.auth.service.CustomUserDetailsService;
import com.workerpay.payroll.service.PaymentPeriodService;
import com.workerpay.report.dto.FinancialSummaryDTO;
import com.workerpay.report.dto.ReportFilterForm;
import com.workerpay.report.dto.ReportTotalsDTO;
import com.workerpay.report.service.ReportService;
import com.workerpay.worker.service.WorkerService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private WorkerService workerService;

    @MockBean
    private PaymentPeriodService paymentPeriodService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @WithMockUser(roles = "ADMIN")
    @Test
    void reportsIndexShouldReturnOk() throws Exception {
        mockMvc.perform(get("/reports"))
            .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void payrollByPeriodShouldReturnOk() throws Exception {
        when(paymentPeriodService.findAllPeriods()).thenReturn(List.of());
        when(reportService.getPayrollByPeriodReport(org.mockito.ArgumentMatchers.any(ReportFilterForm.class))).thenReturn(List.of());
        when(reportService.getPayrollByPeriodTotals(org.mockito.ArgumentMatchers.any(ReportFilterForm.class))).thenReturn(totals());

        mockMvc.perform(get("/reports/payroll-by-period"))
            .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void pendingAdvancesShouldReturnOk() throws Exception {
        when(workerService.findAll()).thenReturn(List.of());
        when(reportService.getPendingAdvancesReport(org.mockito.ArgumentMatchers.any(ReportFilterForm.class))).thenReturn(List.of());
        when(reportService.getPendingAdvancesTotals(org.mockito.ArgumentMatchers.any(ReportFilterForm.class))).thenReturn(totals());

        mockMvc.perform(get("/reports/advances-pending"))
            .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void activeDebtsShouldReturnOk() throws Exception {
        when(workerService.findAll()).thenReturn(List.of());
        when(reportService.getActiveDebtsReport(org.mockito.ArgumentMatchers.any(ReportFilterForm.class))).thenReturn(List.of());
        when(reportService.getActiveDebtsTotals(org.mockito.ArgumentMatchers.any(ReportFilterForm.class))).thenReturn(totals());

        mockMvc.perform(get("/reports/debts-active"))
            .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void financialSummaryShouldReturnOk() throws Exception {
        when(reportService.getFinancialSummary()).thenReturn(summary());

        mockMvc.perform(get("/reports/financial-summary"))
            .andExpect(status().isOk());
    }

    private ReportTotalsDTO totals() {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        return new ReportTotalsDTO(0, zero, zero, zero, zero, zero, zero, zero, zero);
    }

    private FinancialSummaryDTO summary() {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        return new FinancialSummaryDTO(0, 0, zero, 0, zero, 0, zero, zero, zero, List.of(), List.of(), List.of());
    }
}
