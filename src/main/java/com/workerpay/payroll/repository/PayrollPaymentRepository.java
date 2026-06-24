package com.workerpay.payroll.repository;

import com.workerpay.payroll.entity.PayrollPayment;
import com.workerpay.payroll.entity.PayrollPaymentStatus;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayrollPaymentRepository extends JpaRepository<PayrollPayment, Long> {

    @EntityGraph(attributePaths = {"worker", "period"})
    List<PayrollPayment> findAllByOrderByPaymentDateDesc();

    List<PayrollPayment> findByStatus(PayrollPaymentStatus status);

    @EntityGraph(attributePaths = {"worker", "period"})
    @Query("select p from PayrollPayment p where p.status = :status")
    List<PayrollPayment> findWithRelationsByStatus(PayrollPaymentStatus status);

    List<PayrollPayment> findByWorkerId(Long workerId);

    @EntityGraph(attributePaths = {"worker", "period"})
    @Query("select p from PayrollPayment p where p.worker.id = :workerId")
    List<PayrollPayment> findWithRelationsByWorkerId(Long workerId);

    @EntityGraph(attributePaths = {"worker", "period"})
    List<PayrollPayment> findByPeriodId(Long periodId);

    @EntityGraph(attributePaths = {"worker", "period"})
    List<PayrollPayment> findTop5ByOrderByPaymentDateDesc();

    long countByStatus(PayrollPaymentStatus status);

    boolean existsByWorkerIdAndPeriodIdAndStatusNot(Long workerId, Long periodId, PayrollPaymentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PayrollPayment p where p.id = :id")
    Optional<PayrollPayment> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select coalesce(sum(p.netPayment), 0)
        from PayrollPayment p
        where p.period.id = :periodId and p.status = :status
        """)
    BigDecimal sumNetPaymentByPeriodIdAndStatus(
        @Param("periodId") Long periodId,
        @Param("status") PayrollPaymentStatus status
    );

    @Query("""
        select p from PayrollPayment p
        join fetch p.worker
        join fetch p.period
        where (:periodId is null or p.period.id = :periodId)
            and (:status is null or p.status = :status)
            and (:startDate is null or p.paymentDate >= :startDate)
            and (:endDate is null or p.paymentDate <= :endDate)
        order by p.paymentDate desc
        """)
    List<PayrollPayment> findFilteredForReport(
        @Param("periodId") Long periodId,
        @Param("status") PayrollPaymentStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
