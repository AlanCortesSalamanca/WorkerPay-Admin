package com.workerpay.worker.dto;

import com.workerpay.worker.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class WorkerForm {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre completo no puede superar 150 caracteres")
    private String fullName;

    @Size(max = 30, message = "El telefono no puede superar 30 caracteres")
    private String phone;

    @NotBlank(message = "El puesto es obligatorio")
    @Size(max = 100, message = "El puesto no puede superar 100 caracteres")
    private String position;

    @NotNull(message = "El tipo de pago es obligatorio")
    private PaymentType paymentType;

    @NotNull(message = "El salario base es obligatorio")
    @DecimalMin(value = "0.00", message = "El salario base debe ser mayor o igual a 0")
    @Digits(integer = 10, fraction = 2, message = "El salario base no puede superar 10 enteros y 2 decimales")
    private BigDecimal baseSalary;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate hireDate;

    private boolean active = true;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
