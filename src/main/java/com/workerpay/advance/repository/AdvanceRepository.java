package com.workerpay.advance.repository;

import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    List<Advance> findByStatus(AdvanceStatus status);

    List<Advance> findByWorkerId(Long workerId);

    long countByStatus(AdvanceStatus status);
}
