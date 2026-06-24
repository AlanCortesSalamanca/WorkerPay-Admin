package com.workerpay.report.controller;

import com.workerpay.payroll.entity.PaymentPeriod;
import com.workerpay.payroll.entity.PayrollPaymentStatus;
import com.workerpay.payroll.service.PaymentPeriodService;
import com.workerpay.report.dto.ReportFilterForm;
import com.workerpay.report.service.ReportService;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.service.WorkerService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final WorkerService workerService;
    private final PaymentPeriodService paymentPeriodService;

    public ReportController(
        ReportService reportService,
        WorkerService workerService,
        PaymentPeriodService paymentPeriodService
    ) {
        this.reportService = reportService;
        this.workerService = workerService;
        this.paymentPeriodService = paymentPeriodService;
    }

    @GetMapping("/reports")
    public String index() {
        return "reports/index";
    }

    @GetMapping("/reports/payroll-by-period")
    public String payrollByPeriod(@Valid @ModelAttribute ReportFilterForm filter, Model model) {
        model.addAttribute("filter", filter);
        model.addAttribute("periods", periods());
        model.addAttribute("statuses", PayrollPaymentStatus.values());
        model.addAttribute("rows", reportService.getPayrollByPeriodReport(filter));
        model.addAttribute("totals", reportService.getPayrollByPeriodTotals(filter));
        return "reports/payroll-by-period";
    }

    @GetMapping("/reports/advances-pending")
    public String advancesPending(@Valid @ModelAttribute ReportFilterForm filter, Model model) {
        model.addAttribute("filter", filter);
        model.addAttribute("workers", workers());
        model.addAttribute("rows", reportService.getPendingAdvancesReport(filter));
        model.addAttribute("totals", reportService.getPendingAdvancesTotals(filter));
        return "reports/advances-pending";
    }

    @GetMapping("/reports/debts-active")
    public String debtsActive(@Valid @ModelAttribute ReportFilterForm filter, Model model) {
        model.addAttribute("filter", filter);
        model.addAttribute("workers", workers());
        model.addAttribute("rows", reportService.getActiveDebtsReport(filter));
        model.addAttribute("totals", reportService.getActiveDebtsTotals(filter));
        return "reports/debts-active";
    }

    @GetMapping("/reports/worker-history")
    public String workerHistory(@Valid @ModelAttribute ReportFilterForm filter, Model model) {
        model.addAttribute("filter", filter);
        model.addAttribute("workers", workers());
        if (filter.getWorkerId() == null) {
            model.addAttribute("emptyMessage", "Selecciona un trabajador para consultar su historial financiero");
        } else {
            model.addAttribute("history", reportService.getWorkerFinancialHistory(filter.getWorkerId()));
        }
        return "reports/worker-history";
    }

    @GetMapping("/reports/financial-summary")
    public String financialSummary(Model model) {
        model.addAttribute("summary", reportService.getFinancialSummary());
        return "reports/financial-summary";
    }

    @GetMapping("/reports/export/payroll-by-period")
    public ResponseEntity<String> exportPayrollByPeriod(@Valid @ModelAttribute ReportFilterForm filter) {
        return csv("pagos-por-periodo.csv", reportService.exportPayrollByPeriodCsv(filter));
    }

    @GetMapping("/reports/export/advances-pending")
    public ResponseEntity<String> exportPendingAdvances(@Valid @ModelAttribute ReportFilterForm filter) {
        return csv("adelantos-pendientes.csv", reportService.exportPendingAdvancesCsv(filter));
    }

    @GetMapping("/reports/export/debts-active")
    public ResponseEntity<String> exportActiveDebts(@Valid @ModelAttribute ReportFilterForm filter) {
        return csv("deudas-activas.csv", reportService.exportActiveDebtsCsv(filter));
    }

    @GetMapping("/reports/export/worker-history")
    public ResponseEntity<String> exportWorkerHistory(@RequestParam(required = false) Long workerId) {
        String csv = workerId == null
            ? "Selecciona un trabajador para exportar su historial financiero" + System.lineSeparator()
            : reportService.exportWorkerHistoryCsv(workerId);
        return csv("historial-trabajador.csv", csv);
    }

    @GetMapping("/reports/export/financial-summary")
    public ResponseEntity<String> exportFinancialSummary() {
        return csv("resumen-financiero.csv", reportService.exportFinancialSummaryCsv());
    }

    private ResponseEntity<String> csv(String filename, String body) {
        MediaType csvType = new MediaType("text", "csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .contentType(csvType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(body);
    }

    private List<Worker> workers() {
        return workerService.findAll();
    }

    private List<PaymentPeriod> periods() {
        return paymentPeriodService.findAllPeriods();
    }
}
