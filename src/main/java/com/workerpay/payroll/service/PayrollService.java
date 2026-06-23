package com.workerpay.payroll.service;

import com.workerpay.payroll.dto.PayrollPaymentForm;
import com.workerpay.payroll.entity.PayrollPayment;
import java.math.BigDecimal;
import java.util.List;

public interface PayrollService {

    List<PayrollPayment> findAllPayments();

    PayrollPayment findPaymentById(Long id);

    PayrollPayment createPayment(PayrollPaymentForm form);

    PayrollPayment updatePayment(Long id, PayrollPaymentForm form);

    void cancelPayment(Long id);

    boolean markAsPaid(Long id);

    long countPendingPayments();

    BigDecimal calculateNetPayment(
        BigDecimal baseAmount,
        BigDecimal bonuses,
        BigDecimal advanceDiscount,
        BigDecimal debtDiscount,
        BigDecimal otherDiscounts
    );

    BigDecimal getSuggestedAdvanceDiscount(Long workerId);

    BigDecimal getPaidTotalForCurrentOpenPeriod();

    PayrollPaymentForm toForm(PayrollPayment payment);
}
