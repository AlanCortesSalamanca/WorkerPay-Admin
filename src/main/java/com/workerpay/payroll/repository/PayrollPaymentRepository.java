package com.workerpay.payroll.repository;

import com.workerpay.payroll.entity.PayrollPayment;
import com.workerpay.payroll.entity.PayrollPaymentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollPaymentRepository extends JpaRepository<PayrollPayment, Long> {

    List<PayrollPayment> findByStatus(PayrollPaymentStatus status);

    List<PayrollPayment> findByWorkerId(Long workerId);

    List<PayrollPayment> findByPeriodId(Long periodId);

    long countByStatus(PayrollPaymentStatus status);

    boolean existsByWorkerIdAndPeriodIdAndStatusNot(Long workerId, Long periodId, PayrollPaymentStatus status);
}
