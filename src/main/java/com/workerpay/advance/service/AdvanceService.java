package com.workerpay.advance.service;

import com.workerpay.advance.dto.AdvanceForm;
import com.workerpay.advance.entity.Advance;
import java.util.List;

public interface AdvanceService {

    List<Advance> findAll();

    Advance findById(Long id);

    Advance create(AdvanceForm form);

    Advance update(Long id, AdvanceForm form);

    void cancel(Long id);

    void markAsDiscounted(Long id);

    List<Advance> findPending();

    long countPending();

    AdvanceForm toForm(Advance advance);
}
