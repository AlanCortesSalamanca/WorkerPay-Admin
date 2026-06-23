package com.workerpay.debt.controller;

import com.workerpay.debt.dto.DebtForm;
import com.workerpay.debt.dto.DebtPaymentForm;
import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtStatus;
import com.workerpay.debt.service.DebtService;
import com.workerpay.worker.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/debts")
public class DebtController {

    private final DebtService debtService;
    private final WorkerService workerService;

    public DebtController(DebtService debtService, WorkerService workerService) {
        this.debtService = debtService;
        this.workerService = workerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("debts", debtService.findAll());
        return "debts/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("debtForm", new DebtForm());
        prepareDebtForm(model, "Nueva deuda", "/debts");
        return "debts/form";
    }

    @PostMapping
    public String create(
        @Valid @ModelAttribute("debtForm") DebtForm debtForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareDebtForm(model, "Nueva deuda", "/debts");
            return "debts/form";
        }
        debtService.create(debtForm);
        redirectAttributes.addFlashAttribute("successMessage", "Deuda creada correctamente.");
        return "redirect:/debts";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("debt", debtService.findById(id));
        model.addAttribute("payments", debtService.findPayments(id));
        model.addAttribute("activeStatus", DebtStatus.ACTIVE);
        return "debts/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Debt debt = debtService.findById(id);
        if (debt.getStatus() != DebtStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Solo se pueden editar deudas activas.");
            return "redirect:/debts/" + id;
        }
        model.addAttribute("debtForm", debtService.toForm(debt));
        prepareDebtForm(model, "Editar deuda", "/debts/" + id + "/edit");
        return "debts/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("debtForm") DebtForm debtForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareDebtForm(model, "Editar deuda", "/debts/" + id + "/edit");
            return "debts/form";
        }
        try {
            debtService.update(id, debtForm);
            redirectAttributes.addFlashAttribute("successMessage", "Deuda actualizada correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/debts/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            debtService.cancel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Deuda cancelada correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/debts/" + id;
    }

    @GetMapping("/{id}/payments/new")
    public String paymentForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Debt debt = debtService.findById(id);
        if (debt.getStatus() != DebtStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Solo se pueden registrar abonos en deudas activas.");
            return "redirect:/debts/" + id;
        }
        model.addAttribute("debt", debt);
        model.addAttribute("debtPaymentForm", new DebtPaymentForm());
        return "debts/payment-form";
    }

    @PostMapping("/{id}/payments")
    public String addPayment(
        @PathVariable Long id,
        @Valid @ModelAttribute("debtPaymentForm") DebtPaymentForm debtPaymentForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Debt debt = debtService.findById(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("debt", debt);
            return "debts/payment-form";
        }
        try {
            debtService.addPayment(id, debtPaymentForm);
            redirectAttributes.addFlashAttribute("successMessage", "Abono registrado correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/debts/" + id + "/payments/new";
        }
        return "redirect:/debts/" + id;
    }

    private void prepareDebtForm(Model model, String title, String action) {
        model.addAttribute("workers", workerService.findActive());
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
    }
}
