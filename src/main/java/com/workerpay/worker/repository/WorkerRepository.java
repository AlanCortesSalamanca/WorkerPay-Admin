package com.workerpay.worker.repository;

import com.workerpay.worker.entity.Worker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    List<Worker> findByActiveTrueOrderByFullNameAsc();

    long countByActiveTrue();
}
