package com.payment.gateway.infrastructure.adapter.outbound.gateway.dto;

public class ExternalPaymentResponseDTO {
    private String transactionId;
    private String status;

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}