package com.workerpay.payroll.service;

import com.workerpay.payroll.dto.PaymentPeriodForm;
import com.workerpay.payroll.entity.PaymentPeriod;
import java.util.List;

public interface PaymentPeriodService {

    List<PaymentPeriod> findAllPeriods();

    List<PaymentPeriod> findOpenPeriods();

    PaymentPeriod findPeriodById(Long id);

    PaymentPeriod createPeriod(PaymentPeriodForm form);

    PaymentPeriod updatePeriod(Long id, PaymentPeriodForm form);

    void closePeriod(Long id);

    long countOpenPeriods();

    PaymentPeriodForm toForm(PaymentPeriod period);
}
