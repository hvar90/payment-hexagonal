package com.payment.gateway.domain.port.outbound;

import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PaymentRepositoryPort {
    Mono<Payment> save(Payment payment);
    Mono<Payment> findById(String id);
    Mono<Payment> findByReference(String reference);
    Flux<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdAt);
}