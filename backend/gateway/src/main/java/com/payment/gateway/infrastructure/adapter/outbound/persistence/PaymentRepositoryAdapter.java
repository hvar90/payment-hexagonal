package com.payment.gateway.infrastructure.adapter.outbound.persistence;

import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import com.payment.gateway.domain.port.outbound.PaymentRepositoryPort;
import com.payment.gateway.infrastructure.adapter.outbound.persistence.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final SpringDataPaymentRepository repository;

    @Override
    public Mono<Payment> save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Payment> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    private PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setReference(payment.getReference());
        entity.setAmount(payment.getAmount());
        entity.setCurrency(payment.getCurrency());
        entity.setStatus(payment.getStatus());
        entity.setDescription(payment.getDescription());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    private Payment toDomain(PaymentEntity entity) {
        Payment payment = new Payment(
                entity.getId(),
                entity.getReference(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getDescription()
        );
        if (entity.getStatus() != PaymentStatus.PENDING) {
            payment.updateStatus(entity.getStatus());
        }
        return payment;
    }
}