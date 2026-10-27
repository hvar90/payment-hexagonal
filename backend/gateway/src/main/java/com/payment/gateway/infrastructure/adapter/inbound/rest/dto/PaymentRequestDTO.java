package com.payment.gateway.infrastructure.adapter.inbound.rest.dto;

import java.math.BigDecimal;

public class PaymentRequestDTO {
    private String id;
    private String reference;
    private BigDecimal amount;
    private String currency;
    private String description;
    
    public String getId() { return id; }
    public String getReference() { return reference; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
}