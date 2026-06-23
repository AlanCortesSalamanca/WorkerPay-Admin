package com.workerpay.debt.repository;

import com.workerpay.debt.entity.DebtPayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {

    List<DebtPayment> findByDebtIdOrderByPaymentDateDesc(Long debtId);

    List<DebtPayment> findByDebtWorkerId(Long workerId);
}
