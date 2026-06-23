package com.workerpay.advance.service;

import com.workerpay.advance.dto.AdvanceForm;
import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import com.workerpay.advance.repository.AdvanceRepository;
import com.workerpay.common.exception.ResourceNotFoundException;
import com.workerpay.common.util.MoneyUtils;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.service.WorkerService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AdvanceServiceImpl implements AdvanceService {

    private final AdvanceRepository advanceRepository;
    private final WorkerService workerService;

    public AdvanceServiceImpl(AdvanceRepository advanceRepository, WorkerService workerService) {
        this.advanceRepository = advanceRepository;
        this.workerService = workerService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advance> findAll() {
        return advanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Advance findById(Long id) {
        return advanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Adelanto no encontrado"));
    }

    @Override
    public Advance create(AdvanceForm form) {
        Advance advance = new Advance();
        applyForm(advance, form);
        advance.setStatus(AdvanceStatus.PENDING);
        return advanceRepository.save(advance);
    }

    @Override
    public Advance update(Long id, AdvanceForm form) {
        Advance advance = findById(id);
        requirePending(advance, "Solo se pueden editar adelantos pendientes.");
        applyForm(advance, form);
        return advanceRepository.save(advance);
    }

    @Override
    public void cancel(Long id) {
        Advance advance = findById(id);
        if (advance.getStatus() == AdvanceStatus.DISCOUNTED) {
            throw new IllegalStateException("No se puede cancelar un adelanto ya descontado.");
        }
        if (advance.getStatus() == AdvanceStatus.CANCELLED) {
            return;
        }
        advance.setStatus(AdvanceStatus.CANCELLED);
        advanceRepository.save(advance);
    }

    @Override
    public void markAsDiscounted(Long id) {
        Advance advance = findById(id);
        if (advance.getStatus() == AdvanceStatus.CANCELLED) {
            throw new IllegalStateException("No se puede marcar como descontado un adelanto cancelado.");
        }
        if (advance.getStatus() == AdvanceStatus.DISCOUNTED) {
            return;
        }
        advance.setStatus(AdvanceStatus.DISCOUNTED);
        advanceRepository.save(advance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advance> findPending() {
        return advanceRepository.findByStatus(AdvanceStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPending() {
        return advanceRepository.countByStatus(AdvanceStatus.PENDING);
    }

    @Override
    public AdvanceForm toForm(Advance advance) {
        AdvanceForm form = new AdvanceForm();
        form.setWorkerId(advance.getWorker().getId());
        form.setAmount(advance.getAmount());
        form.setDate(advance.getDate());
        form.setReason(advance.getReason());
        return form;
    }

    private void applyForm(Advance advance, AdvanceForm form) {
        Worker worker = workerService.findById(form.getWorkerId());
        advance.setWorker(worker);
        advance.setAmount(MoneyUtils.normalize(form.getAmount()));
        advance.setDate(form.getDate());
        advance.setReason(StringUtils.hasText(form.getReason()) ? form.getReason().trim() : null);
    }

    private void requirePending(Advance advance, String message) {
        if (advance.getStatus() != AdvanceStatus.PENDING) {
            throw new IllegalStateException(message);
        }
    }
}
