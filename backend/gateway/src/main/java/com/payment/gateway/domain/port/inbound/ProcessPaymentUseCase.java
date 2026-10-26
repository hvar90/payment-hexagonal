package com.payment.gateway.domain.port.inbound;

import com.payment.gateway.domain.model.Payment;

import reactor.core.publisher.Mono;

public interface ProcessPaymentUseCase {
    Mono<Payment> processPayment(Payment payment);
}