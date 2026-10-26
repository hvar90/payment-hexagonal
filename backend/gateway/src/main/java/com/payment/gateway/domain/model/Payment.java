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
        this.status = PaymentStatus.APPROVED;
    }

    public void reject() {
        this.status = PaymentStatus.REJECTED;
    }

  
    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}