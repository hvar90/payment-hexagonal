package com.payment.gateway.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private String id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String description;
    private LocalDateTime createdAt;

    public Payment(String id, BigDecimal amount, String currency, String description) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public void approve() {
        if (this.status == PaymentStatus.APPROVED) {
            return; // Ya está aprobado (Idempotencia a nivel de dominio)
        }
        if (this.status == PaymentStatus.REJECTED || this.status == PaymentStatus.FAILED) {
            throw new IllegalStateException("No se puede aprobar un pago que ya se encuentra en estado: " + this.status);
        }
        this.status = PaymentStatus.APPROVED;
    }

    public void reject() {
        if (this.status == PaymentStatus.REJECTED) {
            return; // Ya está rechazado (Idempotencia a nivel de dominio)
        }
        if (this.status == PaymentStatus.APPROVED) {
            throw new IllegalStateException("No se puede rechazar un pago que ya ha sido aprobado.");
        }
        this.status = PaymentStatus.REJECTED;
    }

    public void updateStatus(PaymentStatus status) {
        if (status == PaymentStatus.APPROVED) {
            approve();
        } else if (status == PaymentStatus.REJECTED) {
            reject();
        } else {
            this.status = status;
        }
    }

    // Getters
    public String getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}