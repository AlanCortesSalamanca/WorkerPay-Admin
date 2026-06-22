package com.workerpay.worker.controller;

import com.workerpay.worker.dto.WorkerForm;
import com.workerpay.worker.entity.PaymentType;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping("/workers")
    public String list(Model model) {
        model.addAttribute("workers", workerService.findAll());
        return "workers/list";
    }

    @GetMapping("/workers/new")
    public String createForm(Model model) {
        model.addAttribute("workerForm", new WorkerForm());
        model.addAttribute("paymentTypes", PaymentType.values());
        model.addAttribute("formTitle", "Nuevo trabajador");
        model.addAttribute("formAction", "/workers");
        return "workers/form";
    }

    @PostMapping("/workers")
    public String create(
        @Valid @ModelAttribute("workerForm") WorkerForm workerForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Nuevo trabajador", "/workers");
            return "workers/form";
        }
        workerService.create(workerForm);
        redirectAttributes.addFlashAttribute("successMessage", "Trabajador creado correctamente.");
        return "redirect:/workers";
    }

    @GetMapping("/workers/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("worker", workerService.findById(id));
        return "workers/detail";
    }

    @GetMapping("/workers/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Worker worker = workerService.findById(id);
        model.addAttribute("workerForm", workerService.toForm(worker));
        prepareForm(model, "Editar trabajador", "/workers/" + id);
        return "workers/form";
    }

    @PostMapping("/workers/{id}")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("workerForm") WorkerForm workerForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Editar trabajador", "/workers/" + id);
            return "workers/form";
        }
        workerService.update(id, workerForm);
        redirectAttributes.addFlashAttribute("successMessage", "Trabajador actualizado correctamente.");
        return "redirect:/workers/" + id;
    }

    @PostMapping("/workers/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        workerService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Trabajador desactivado correctamente.");
        return "redirect:/workers";
    }

    @PostMapping("/workers/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        workerService.activate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Trabajador activado correctamente.");
        return "redirect:/workers";
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("paymentTypes", PaymentType.values());
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
    }
}
