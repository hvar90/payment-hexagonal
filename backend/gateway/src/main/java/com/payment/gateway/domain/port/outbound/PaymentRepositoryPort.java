package com.payment.gateway.domain.port.outbound;

import com.payment.gateway.domain.model.Payment;

import reactor.core.publisher.Mono;

public interface PaymentRepositoryPort {
    Mono<Payment> save(Payment payment);
    Mono<Payment> findById(String id);
    Mono<Payment> findByReference(String reference);
}