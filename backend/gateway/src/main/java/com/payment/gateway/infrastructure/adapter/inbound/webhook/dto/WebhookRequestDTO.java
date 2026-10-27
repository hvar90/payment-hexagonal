package com.payment.gateway.infrastructure.adapter.inbound.webhook.dto;

import com.payment.gateway.domain.model.PaymentStatus;

public class WebhookRequestDTO {
    private String eventId;
    private String paymentId;
    private PaymentStatus status;
    
    // Getters
    public String getEventId() { return eventId; }
    public String getPaymentId() { return paymentId; }
    public PaymentStatus getStatus() { return status; }
}