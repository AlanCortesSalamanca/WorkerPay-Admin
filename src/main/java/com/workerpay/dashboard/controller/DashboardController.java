package com.workerpay.dashboard.controller;

import com.workerpay.advance.service.AdvanceService;
import com.workerpay.debt.service.DebtService;
import com.workerpay.payroll.service.PayrollService;
import com.workerpay.worker.service.WorkerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final WorkerService workerService;
    private final AdvanceService advanceService;
    private final DebtService debtService;
    private final PayrollService payrollService;

    public DashboardController(
        WorkerService workerService,
        AdvanceService advanceService,
        DebtService debtService,
        PayrollService payrollService
    ) {
        this.workerService = workerService;
        this.advanceService = advanceService;
        this.debtService = debtService;
        this.payrollService = payrollService;
    }

    @GetMapping({"/", "/dashboard"})
    public String index(Model model) {
        model.addAttribute("activeWorkers", workerService.countActive());
        model.addAttribute("pendingAdvances", advanceService.countPending());
        model.addAttribute("activeDebts", debtService.countActive());
        model.addAttribute("pendingPayments", payrollService.countPendingPayments());
        model.addAttribute("periodPaidTotal", payrollService.getPaidTotalForCurrentOpenPeriod());
        return "dashboard/index";
    }
}
