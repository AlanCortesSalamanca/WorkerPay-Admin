package com.workerpay.debt.service;

import com.workerpay.debt.dto.DebtForm;
import com.workerpay.debt.dto.DebtPaymentForm;
import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtPayment;
import java.util.List;

public interface DebtService {

    List<Debt> findAll();

    Debt findById(Long id);

    Debt create(DebtForm form);

    Debt update(Long id, DebtForm form);

    void cancel(Long id);

    DebtPayment addPayment(Long debtId, DebtPaymentForm form);

    List<DebtPayment> findPayments(Long debtId);

    List<Debt> findActive();

    List<Debt> findActiveByWorkerId(Long workerId);

    long countActive();

    DebtForm toForm(Debt debt);
}
