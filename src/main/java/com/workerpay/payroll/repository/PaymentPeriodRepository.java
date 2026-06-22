package com.workerpay.payroll.repository;

import com.workerpay.payroll.entity.PaymentPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentPeriodRepository extends JpaRepository<PaymentPeriod, Long> {
}
