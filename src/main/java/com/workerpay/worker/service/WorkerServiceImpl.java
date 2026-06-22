package com.workerpay.worker.service;

import com.workerpay.common.exception.ResourceNotFoundException;
import com.workerpay.common.util.MoneyUtils;
import com.workerpay.worker.dto.WorkerForm;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.repository.WorkerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;

    public WorkerServiceImpl(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> findAll() {
        return workerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Worker findById(Long id) {
        return workerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado"));
    }

    @Override
    public Worker create(WorkerForm form) {
        Worker worker = new Worker();
        applyForm(worker, form);
        return workerRepository.save(worker);
    }

    @Override
    public Worker update(Long id, WorkerForm form) {
        Worker worker = findById(id);
        applyForm(worker, form);
        return workerRepository.save(worker);
    }

    @Override
    public void deactivate(Long id) {
        Worker worker = findById(id);
        worker.setActive(false);
        workerRepository.save(worker);
    }

    @Override
    public void activate(Long id) {
        Worker worker = findById(id);
        worker.setActive(true);
        workerRepository.save(worker);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return workerRepository.countByActiveTrue();
    }

    @Override
    public WorkerForm toForm(Worker worker) {
        WorkerForm form = new WorkerForm();
        form.setFullName(worker.getFullName());
        form.setPhone(worker.getPhone());
        form.setPosition(worker.getPosition());
        form.setPaymentType(worker.getPaymentType());
        form.setBaseSalary(worker.getBaseSalary());
        form.setHireDate(worker.getHireDate());
        form.setActive(worker.isActive());
        return form;
    }

    private void applyForm(Worker worker, WorkerForm form) {
        worker.setFullName(form.getFullName().trim());
        worker.setPhone(form.getPhone());
        worker.setPosition(form.getPosition().trim());
        worker.setPaymentType(form.getPaymentType());
        worker.setBaseSalary(MoneyUtils.normalize(form.getBaseSalary()));
        worker.setHireDate(form.getHireDate());
        worker.setActive(form.isActive());
    }
}
