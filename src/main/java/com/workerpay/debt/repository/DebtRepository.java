package com.workerpay.debt.repository;

import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByStatus(DebtStatus status);

    List<Debt> findByWorkerId(Long workerId);

    List<Debt> findByWorkerIdAndStatus(Long workerId, DebtStatus status);

    long countByStatus(DebtStatus status);
}
