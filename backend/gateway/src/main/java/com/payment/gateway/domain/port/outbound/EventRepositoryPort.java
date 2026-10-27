package com.payment.gateway.domain.port.outbound;

import reactor.core.publisher.Mono;

public interface EventRepositoryPort {
    Mono<Boolean> existsByEventId(String eventId);
    Mono<Void> saveProcessedEvent(String eventId, String paymentId, String eventType, String status);
}