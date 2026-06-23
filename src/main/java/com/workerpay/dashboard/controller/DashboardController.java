package com.workerpay.dashboard.controller;

import com.workerpay.advance.service.AdvanceService;
import com.workerpay.worker.service.WorkerService;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final WorkerService workerService;
    private final AdvanceService advanceService;

    public DashboardController(WorkerService workerService, AdvanceService advanceService) {
        this.workerService = workerService;
        this.advanceService = advanceService;
    }

    @GetMapping({"/", "/dashboard"})
    public String index(Model model) {
        model.addAttribute("activeWorkers", workerService.countActive());
        model.addAttribute("pendingAdvances", advanceService.countPending());
        model.addAttribute("activeDebts", 0);
        model.addAttribute("pendingPayments", 0);
        model.addAttribute("periodPaidTotal", BigDecimal.ZERO);
        return "dashboard/index";
    }
}
