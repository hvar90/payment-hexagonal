package com.payment.gateway.infrastructure.adapter.inbound.webhook.dto;

import com.payment.gateway.domain.model.PaymentStatus;

public class WebhookRequestDTO {
    private String paymentId;
    private PaymentStatus status;
    
    // Getters necesarios para que Jackson pueda deserializar el JSON
    public String getPaymentId() { return paymentId; }
    public PaymentStatus getStatus() { return status; }
}