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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_payments", indexes = {
    @Index(name = "idx_payroll_payments_worker_id", columnList = "worker_id"),
    @Index(name = "idx_payroll_payments_period_id", columnList = "period_id"),
    @Index(name = "idx_payroll_payments_status", columnList = "status"),
    @Index(name = "idx_payroll_payments_worker_period", columnList = "worker_id,period_id")
})
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

    public Long getId() {
        return id;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public PaymentPeriod getPeriod() {
        return period;
    }

    public void setPeriod(PaymentPeriod period) {
        this.period = period;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getBonuses() {
        return bonuses;
    }

    public void setBonuses(BigDecimal bonuses) {
        this.bonuses = bonuses;
    }

    public BigDecimal getAdvanceDiscount() {
        return advanceDiscount;
    }

    public void setAdvanceDiscount(BigDecimal advanceDiscount) {
        this.advanceDiscount = advanceDiscount;
    }

    public BigDecimal getDebtDiscount() {
        return debtDiscount;
    }

    public void setDebtDiscount(BigDecimal debtDiscount) {
        this.debtDiscount = debtDiscount;
    }

    public BigDecimal getOtherDiscounts() {
        return otherDiscounts;
    }

    public void setOtherDiscounts(BigDecimal otherDiscounts) {
        this.otherDiscounts = otherDiscounts;
    }

    public BigDecimal getNetPayment() {
        return netPayment;
    }

    public void setNetPayment(BigDecimal netPayment) {
        this.netPayment = netPayment;
    }

    public PayrollPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollPaymentStatus status) {
        this.status = status;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}
