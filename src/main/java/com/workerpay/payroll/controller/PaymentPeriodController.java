package com.workerpay.payroll.controller;

import com.workerpay.payroll.dto.PaymentPeriodForm;
import com.workerpay.payroll.entity.PaymentPeriod;
import com.workerpay.payroll.entity.PaymentPeriodStatus;
import com.workerpay.payroll.service.PaymentPeriodService;
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
@RequestMapping("/payment-periods")
public class PaymentPeriodController {

    private final PaymentPeriodService paymentPeriodService;

    public PaymentPeriodController(PaymentPeriodService paymentPeriodService) {
        this.paymentPeriodService = paymentPeriodService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("periods", paymentPeriodService.findAllPeriods());
        return "payroll/periods-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("paymentPeriodForm", new PaymentPeriodForm());
        prepareForm(model, "Nuevo periodo", "/payment-periods");
        return "payroll/period-form";
    }

    @PostMapping
    public String create(
        @Valid @ModelAttribute("paymentPeriodForm") PaymentPeriodForm paymentPeriodForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Nuevo periodo", "/payment-periods");
            return "payroll/period-form";
        }
        try {
            paymentPeriodService.createPeriod(paymentPeriodForm);
            redirectAttributes.addFlashAttribute("successMessage", "Periodo creado correctamente.");
            return "redirect:/payment-periods";
        } catch (IllegalStateException exception) {
            bindingResult.reject("period.invalid", exception.getMessage());
            prepareForm(model, "Nuevo periodo", "/payment-periods");
            return "payroll/period-form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("period", paymentPeriodService.findPeriodById(id));
        model.addAttribute("openStatus", PaymentPeriodStatus.OPEN);
        return "payroll/period-detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PaymentPeriod period = paymentPeriodService.findPeriodById(id);
        if (period.getStatus() == PaymentPeriodStatus.CLOSED) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede editar un periodo cerrado.");
            return "redirect:/payment-periods/" + id;
        }
        model.addAttribute("paymentPeriodForm", paymentPeriodService.toForm(period));
        prepareForm(model, "Editar periodo", "/payment-periods/" + id + "/edit");
        return "payroll/period-form";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("paymentPeriodForm") PaymentPeriodForm paymentPeriodForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Editar periodo", "/payment-periods/" + id + "/edit");
            return "payroll/period-form";
        }
        try {
            paymentPeriodService.updatePeriod(id, paymentPeriodForm);
            redirectAttributes.addFlashAttribute("successMessage", "Periodo actualizado correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/payment-periods/" + id;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentPeriodService.closePeriod(id);
        redirectAttributes.addFlashAttribute("successMessage", "Periodo cerrado correctamente.");
        return "redirect:/payment-periods/" + id;
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
    }
}
