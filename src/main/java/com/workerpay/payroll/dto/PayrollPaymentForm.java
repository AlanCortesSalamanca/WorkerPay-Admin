package com.workerpay.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PayrollPaymentForm {

    @NotNull(message = "Selecciona un trabajador.")
    private Long workerId;

    @NotNull(message = "Selecciona un periodo.")
    private Long periodId;

    @NotNull(message = "El sueldo base es obligatorio.")
    @DecimalMin(value = "0.00", message = "El sueldo base debe ser mayor o igual a 0.")
    @Digits(integer = 10, fraction = 2, message = "El sueldo base no puede superar 10 enteros y 2 decimales.")
    private BigDecimal baseAmount;

    @DecimalMin(value = "0.00", message = "Los bonos deben ser mayor o igual a 0.")
    @Digits(integer = 10, fraction = 2, message = "Los bonos no pueden superar 10 enteros y 2 decimales.")
    private BigDecimal bonuses;

    @DecimalMin(value = "0.00", message = "El descuento por adelantos debe ser mayor o igual a 0.")
    @Digits(integer = 10, fraction = 2, message = "El descuento por adelantos no puede superar 10 enteros y 2 decimales.")
    private BigDecimal advanceDiscount;

    @DecimalMin(value = "0.00", message = "El descuento por deuda debe ser mayor o igual a 0.")
    @Digits(integer = 10, fraction = 2, message = "El descuento por deuda no puede superar 10 enteros y 2 decimales.")
    private BigDecimal debtDiscount;

    @DecimalMin(value = "0.00", message = "Otros descuentos deben ser mayor o igual a 0.")
    @Digits(integer = 10, fraction = 2, message = "Otros descuentos no pueden superar 10 enteros y 2 decimales.")
    private BigDecimal otherDiscounts;

    @NotNull(message = "La fecha de pago es obligatoria.")
    private LocalDate paymentDate;

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Long periodId) {
        this.periodId = periodId;
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

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}
