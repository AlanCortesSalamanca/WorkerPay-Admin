package com.workerpay.debt.repository;

import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtStatus;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DebtRepository extends JpaRepository<Debt, Long> {

    @EntityGraph(attributePaths = "worker")
    List<Debt> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "worker")
    List<Debt> findByStatus(DebtStatus status);

    @EntityGraph(attributePaths = "worker")
    List<Debt> findByWorkerId(Long workerId);

    List<Debt> findByWorkerIdAndStatus(Long workerId, DebtStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Debt d where d.id = :id")
    Optional<Debt> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Debt d where d.worker.id = :workerId and d.status = :status order by d.createdAt asc")
    List<Debt> findByWorkerIdAndStatusForUpdate(@Param("workerId") Long workerId, @Param("status") DebtStatus status);

    @EntityGraph(attributePaths = "worker")
    List<Debt> findTop5ByStatusOrderByCreatedAtDesc(DebtStatus status);

    long countByStatus(DebtStatus status);

    @Query("""
        select d from Debt d
        join fetch d.worker
        where d.status = :status
            and (:workerId is null or d.worker.id = :workerId)
            and (:minAmount is null or d.currentBalance >= :minAmount)
            and (:maxAmount is null or d.currentBalance <= :maxAmount)
        order by d.createdAt desc
        """)
    List<Debt> findFilteredForReport(
        @Param("status") DebtStatus status,
        @Param("workerId") Long workerId,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount
    );
}
