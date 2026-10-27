package com.payment.gateway.infrastructure.adapter.outbound.persistence;

import com.payment.gateway.domain.port.outbound.EventRepositoryPort;
import com.payment.gateway.infrastructure.adapter.outbound.persistence.entity.ProcessedEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventRepositoryAdapter implements EventRepositoryPort {

    private final SpringDataProcessedEventRepository repository;

    @Override
    public Mono<Boolean> existsByEventId(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public Mono<Void> saveProcessedEvent(String eventId, String paymentId, String eventType, String status) {
        ProcessedEventEntity entity = new ProcessedEventEntity(
                eventId,
                paymentId,
                eventType,
                status,
                LocalDateTime.now()
        );
        return repository.save(entity).then();
    }
}