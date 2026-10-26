package com.payment.gateway.infrastructure.adapter.inbound.rest.exception;

public class MalformedWebhookPayloadException extends RuntimeException {
    public MalformedWebhookPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}