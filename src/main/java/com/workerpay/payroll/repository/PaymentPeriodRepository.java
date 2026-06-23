package com.workerpay.payroll.repository;

import com.workerpay.payroll.entity.PaymentPeriod;
import com.workerpay.payroll.entity.PaymentPeriodStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentPeriodRepository extends JpaRepository<PaymentPeriod, Long> {

    List<PaymentPeriod> findByStatus(PaymentPeriodStatus status);

    long countByStatus(PaymentPeriodStatus status);
}
