package com.workerpay.advance.repository;

import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    @EntityGraph(attributePaths = "worker")
    List<Advance> findAllByOrderByDateDesc();

    @EntityGraph(attributePaths = "worker")
    List<Advance> findByStatus(AdvanceStatus status);

    @EntityGraph(attributePaths = "worker")
    List<Advance> findByWorkerId(Long workerId);

    List<Advance> findByWorkerIdAndStatus(Long workerId, AdvanceStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Advance a where a.worker.id = :workerId and a.status = :status")
    List<Advance> findByWorkerIdAndStatusForUpdate(@Param("workerId") Long workerId, @Param("status") AdvanceStatus status);

    @EntityGraph(attributePaths = "worker")
    List<Advance> findTop5ByStatusOrderByDateDesc(AdvanceStatus status);

    long countByStatus(AdvanceStatus status);

    @Query("""
        select a from Advance a
        join fetch a.worker
        where a.status = :status
            and (:workerId is null or a.worker.id = :workerId)
            and (:startDate is null or a.date >= :startDate)
            and (:endDate is null or a.date <= :endDate)
        order by a.date desc
        """)
    List<Advance> findFilteredForReport(
        @Param("status") AdvanceStatus status,
        @Param("workerId") Long workerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
