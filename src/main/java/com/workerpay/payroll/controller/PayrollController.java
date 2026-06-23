package com.workerpay.payroll.controller;

import com.workerpay.payroll.dto.PayrollPaymentForm;
import com.workerpay.payroll.entity.PayrollPayment;
import com.workerpay.payroll.entity.PayrollPaymentStatus;
import com.workerpay.payroll.service.PaymentPeriodService;
import com.workerpay.payroll.service.PayrollService;
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
@RequestMapping("/payroll")
public class PayrollController {

    private final PayrollService payrollService;
    private final PaymentPeriodService paymentPeriodService;
    private final WorkerService workerService;

    public PayrollController(
        PayrollService payrollService,
        PaymentPeriodService paymentPeriodService,
        WorkerService workerService
    ) {
        this.payrollService = payrollService;
        this.paymentPeriodService = paymentPeriodService;
        this.workerService = workerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("payments", payrollService.findAllPayments());
        return "payroll/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        PayrollPaymentForm form = new PayrollPaymentForm();
        form.setBonuses(java.math.BigDecimal.ZERO);
        form.setAdvanceDiscount(java.math.BigDecimal.ZERO);
        form.setDebtDiscount(java.math.BigDecimal.ZERO);
        form.setOtherDiscounts(java.math.BigDecimal.ZERO);
        model.addAttribute("payrollPaymentForm", form);
        prepareForm(model, "Nuevo pago", "/payroll");
        return "payroll/form";
    }

    @PostMapping
    public String create(
        @Valid @ModelAttribute("payrollPaymentForm") PayrollPaymentForm payrollPaymentForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Nuevo pago", "/payroll");
            return "payroll/form";
        }
        try {
            PayrollPayment payment = payrollService.createPayment(payrollPaymentForm);
            redirectAttributes.addFlashAttribute("successMessage", "Pago creado correctamente.");
            return "redirect:/payroll/" + payment.getId();
        } catch (IllegalStateException exception) {
            bindingResult.reject("payment.invalid", exception.getMessage());
            prepareForm(model, "Nuevo pago", "/payroll");
            return "payroll/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("payment", payrollService.findPaymentById(id));
        model.addAttribute("pendingStatus", PayrollPaymentStatus.PENDING);
        return "payroll/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PayrollPayment payment = payrollService.findPaymentById(id);
        if (payment.getStatus() != PayrollPaymentStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Solo se pueden editar pagos pendientes.");
            return "redirect:/payroll/" + id;
        }
        model.addAttribute("payrollPaymentForm", payrollService.toForm(payment));
        prepareForm(model, "Editar pago", "/payroll/" + id + "/edit");
        return "payroll/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("payrollPaymentForm") PayrollPaymentForm payrollPaymentForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Editar pago", "/payroll/" + id + "/edit");
            return "payroll/form";
        }
        try {
            payrollService.updatePayment(id, payrollPaymentForm);
            redirectAttributes.addFlashAttribute("successMessage", "Pago actualizado correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/payroll/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            payrollService.cancelPayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pago cancelado correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/payroll/" + id;
    }

    @PostMapping("/{id}/mark-paid")
    public String markPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean advancesMatched = payrollService.markAsPaid(id);
            redirectAttributes.addFlashAttribute("successMessage", advancesMatched
                ? "Pago marcado como pagado correctamente."
                : "Pago marcado como pagado. El descuento de adelantos no coincidio con el total pendiente, asi que no se marcaron adelantos automaticamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/payroll/" + id;
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("workers", workerService.findActive());
        model.addAttribute("periods", paymentPeriodService.findOpenPeriods());
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
    }
}
