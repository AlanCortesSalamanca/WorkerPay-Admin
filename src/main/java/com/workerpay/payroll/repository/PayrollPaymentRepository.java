package com.workerpay.payroll.repository;

import com.workerpay.payroll.entity.PayrollPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollPaymentRepository extends JpaRepository<PayrollPayment, Long> {
}
