package com.workerpay.advance.controller;

import com.workerpay.advance.dto.AdvanceForm;
import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import com.workerpay.advance.service.AdvanceService;
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
@RequestMapping("/advances")
public class AdvanceController {

    private final AdvanceService advanceService;
    private final WorkerService workerService;

    public AdvanceController(AdvanceService advanceService, WorkerService workerService) {
        this.advanceService = advanceService;
        this.workerService = workerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("advances", advanceService.findAll());
        return "advances/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("advanceForm", new AdvanceForm());
        prepareForm(model, "Nuevo adelanto", "/advances");
        return "advances/form";
    }

    @PostMapping
    public String create(
        @Valid @ModelAttribute("advanceForm") AdvanceForm advanceForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Nuevo adelanto", "/advances");
            return "advances/form";
        }
        advanceService.create(advanceForm);
        redirectAttributes.addFlashAttribute("successMessage", "Adelanto creado correctamente.");
        return "redirect:/advances";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("advance", advanceService.findById(id));
        model.addAttribute("pendingStatus", AdvanceStatus.PENDING);
        return "advances/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Advance advance = advanceService.findById(id);
        if (advance.getStatus() != AdvanceStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Solo se pueden editar adelantos pendientes.");
            return "redirect:/advances/" + id;
        }
        model.addAttribute("advanceForm", advanceService.toForm(advance));
        prepareForm(model, "Editar adelanto", "/advances/" + id + "/edit");
        return "advances/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("advanceForm") AdvanceForm advanceForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Editar adelanto", "/advances/" + id + "/edit");
            return "advances/form";
        }
        try {
            advanceService.update(id, advanceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Adelanto actualizado correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/advances/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            advanceService.cancel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Adelanto cancelado correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/advances/" + id;
    }

    @PostMapping("/{id}/mark-discounted")
    public String markDiscounted(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            advanceService.markAsDiscounted(id);
            redirectAttributes.addFlashAttribute("successMessage", "Adelanto marcado como descontado.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/advances/" + id;
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("workers", workerService.findActive());
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
    }
}
