package com.payment.gateway.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.payment.gateway.domain.exception.InvalidPaymentStateException;

public class Payment {
    private String id;
    private String reference;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment(String id, String reference, BigDecimal amount, String currency, String description) {
        this.id = id;
        this.reference = reference;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void approve() {
        if (this.status == PaymentStatus.APPROVED) return;
        if (this.status == PaymentStatus.REJECTED || this.status == PaymentStatus.FAILED) {
            throw new InvalidPaymentStateException("No se puede aprobar un pago en estado: " + this.status);
        }
        this.status = PaymentStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.status == PaymentStatus.REJECTED) return;
        if (this.status == PaymentStatus.APPROVED) {
            throw new InvalidPaymentStateException("No se puede rechazar un pago ya aprobado.");
        }
        this.status = PaymentStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus status) {
        if (status == PaymentStatus.APPROVED) approve();
        else if (status == PaymentStatus.REJECTED) reject();
        else {
            this.status = status;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Getters
    public String getId() { return id; }
    public String getReference() { return reference; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}