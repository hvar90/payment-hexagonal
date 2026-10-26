package com.payment.gateway.infrastructure.adapter.inbound.rest.dto;

import java.math.BigDecimal;

public class PaymentRequestDTO {
    private String id;
    private BigDecimal amount;
    private String currency;
    private String description;
    
    // Getters y Setters
    public String getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
}