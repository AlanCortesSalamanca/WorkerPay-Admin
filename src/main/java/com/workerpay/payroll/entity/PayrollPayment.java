package com.workerpay.payroll.entity;

import com.workerpay.common.entity.BaseEntity;
import com.workerpay.worker.entity.Worker;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_payments")
public class PayrollPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private PaymentPeriod period;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bonuses;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal advanceDiscount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal debtDiscount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal otherDiscounts;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollPaymentStatus status = PayrollPaymentStatus.PENDING;

    private LocalDate paymentDate;
}
