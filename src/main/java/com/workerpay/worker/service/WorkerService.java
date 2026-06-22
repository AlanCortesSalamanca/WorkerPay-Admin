package com.workerpay.worker.service;

import com.workerpay.worker.dto.WorkerForm;
import com.workerpay.worker.entity.Worker;
import java.util.List;

public interface WorkerService {

    List<Worker> findAll();

    Worker findById(Long id);

    Worker create(WorkerForm form);

    Worker update(Long id, WorkerForm form);

    void deactivate(Long id);

    void activate(Long id);

    long countActive();

    WorkerForm toForm(Worker worker);
}
