package com.workerpay.debt.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class DebtForm {

    @NotNull(message = "Selecciona un trabajador.")
    private Long workerId;

    @NotNull(message = "El monto original es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto original debe ser mayor que 0.")
    @Digits(integer = 10, fraction = 2, message = "El monto original no puede superar 10 enteros y 2 decimales.")
    private BigDecimal originalAmount;

    @DecimalMin(value = "0.00", message = "El abono sugerido debe ser mayor o igual a 0.")
    @Digits(integer = 10, fraction = 2, message = "El abono sugerido no puede superar 10 enteros y 2 decimales.")
    private BigDecimal suggestedPayment;

    @Size(max = 255, message = "La descripcion no puede superar 255 caracteres.")
    private String description;

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getSuggestedPayment() {
        return suggestedPayment;
    }

    public void setSuggestedPayment(BigDecimal suggestedPayment) {
        this.suggestedPayment = suggestedPayment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
