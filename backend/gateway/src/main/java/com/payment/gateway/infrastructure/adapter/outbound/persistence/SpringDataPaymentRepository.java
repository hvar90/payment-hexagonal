package com.payment.gateway.infrastructure.adapter.outbound.persistence;

import com.payment.gateway.infrastructure.adapter.outbound.persistence.entity.PaymentEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPaymentRepository extends ReactiveCrudRepository<PaymentEntity, String> {
    
    Mono<PaymentEntity> findByReference(String reference);
    
    Flux<PaymentEntity> findByStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);
}