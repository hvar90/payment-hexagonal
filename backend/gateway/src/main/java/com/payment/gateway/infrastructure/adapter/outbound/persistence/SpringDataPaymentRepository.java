package com.payment.gateway.infrastructure.adapter.outbound.persistence;

import com.payment.gateway.infrastructure.adapter.outbound.persistence.entity.PaymentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPaymentRepository extends ReactiveCrudRepository<PaymentEntity, String> {
}