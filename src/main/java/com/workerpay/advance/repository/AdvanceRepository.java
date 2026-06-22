package com.workerpay.advance.repository;

import com.workerpay.advance.entity.Advance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvanceRepository extends JpaRepository<Advance, Long> {
}
