package com.workerpay.debt.repository;

import com.workerpay.debt.entity.DebtPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {
}
