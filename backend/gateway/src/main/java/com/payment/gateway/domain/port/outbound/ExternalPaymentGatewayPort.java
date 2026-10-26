package com.payment.gateway.domain.port.outbound;

import com.payment.gateway.domain.model.Payment;

import reactor.core.publisher.Mono;

public interface ExternalPaymentGatewayPort {
    Mono<Payment> charge(Payment payment);
}