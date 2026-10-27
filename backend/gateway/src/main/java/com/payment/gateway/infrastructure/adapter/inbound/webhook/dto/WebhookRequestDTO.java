package com.payment.gateway.infrastructure.adapter.inbound.webhook.dto;

import com.payment.gateway.domain.model.PaymentStatus;

public class WebhookRequestDTO {
    private String eventId;
    private String paymentId;
    //transaction.updated, payment.created, charge.failed
    private String eventType; 
    //PENDING, APPROVED, REJECTED
    private PaymentStatus status;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}