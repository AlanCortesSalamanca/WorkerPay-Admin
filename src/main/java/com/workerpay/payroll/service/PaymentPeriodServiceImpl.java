package com.workerpay.payroll.service;

import com.workerpay.common.exception.ResourceNotFoundException;
import com.workerpay.payroll.dto.PaymentPeriodForm;
import com.workerpay.payroll.entity.PaymentPeriod;
import com.workerpay.payroll.entity.PaymentPeriodStatus;
import com.workerpay.payroll.repository.PaymentPeriodRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentPeriodServiceImpl implements PaymentPeriodService {

    private final PaymentPeriodRepository paymentPeriodRepository;

    public PaymentPeriodServiceImpl(PaymentPeriodRepository paymentPeriodRepository) {
        this.paymentPeriodRepository = paymentPeriodRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentPeriod> findAllPeriods() {
        return paymentPeriodRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentPeriod> findOpenPeriods() {
        return paymentPeriodRepository.findByStatus(PaymentPeriodStatus.OPEN);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentPeriod findPeriodById(Long id) {
        return paymentPeriodRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Periodo de pago no encontrado"));
    }

    @Override
    public PaymentPeriod createPeriod(PaymentPeriodForm form) {
        validateDates(form);
        PaymentPeriod period = new PaymentPeriod();
        applyForm(period, form);
        period.setStatus(PaymentPeriodStatus.OPEN);
        return paymentPeriodRepository.save(period);
    }

    @Override
    public PaymentPeriod updatePeriod(Long id, PaymentPeriodForm form) {
        PaymentPeriod period = findPeriodById(id);
        if (period.getStatus() == PaymentPeriodStatus.CLOSED) {
            throw new IllegalStateException("No se puede editar un periodo cerrado.");
        }
        validateDates(form);
        applyForm(period, form);
        return paymentPeriodRepository.save(period);
    }

    @Override
    public void closePeriod(Long id) {
        PaymentPeriod period = findPeriodById(id);
        if (period.getStatus() == PaymentPeriodStatus.CLOSED) {
            return;
        }
        period.setStatus(PaymentPeriodStatus.CLOSED);
        paymentPeriodRepository.save(period);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOpenPeriods() {
        return paymentPeriodRepository.countByStatus(PaymentPeriodStatus.OPEN);
    }

    @Override
    public PaymentPeriodForm toForm(PaymentPeriod period) {
        PaymentPeriodForm form = new PaymentPeriodForm();
        form.setName(period.getName());
        form.setStartDate(period.getStartDate());
        form.setEndDate(period.getEndDate());
        return form;
    }

    private void applyForm(PaymentPeriod period, PaymentPeriodForm form) {
        period.setName(form.getName().trim());
        period.setStartDate(form.getStartDate());
        period.setEndDate(form.getEndDate());
    }

    private void validateDates(PaymentPeriodForm form) {
        if (form.getStartDate() != null && form.getEndDate() != null && form.getEndDate().isBefore(form.getStartDate())) {
            throw new IllegalStateException("La fecha final debe ser igual o posterior a la fecha inicial.");
        }
    }
}
