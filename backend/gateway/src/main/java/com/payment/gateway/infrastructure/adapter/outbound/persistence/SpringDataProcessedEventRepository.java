package com.payment.gateway.infrastructure.adapter.outbound.persistence;

import com.payment.gateway.infrastructure.adapter.outbound.persistence.entity.ProcessedEventEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataProcessedEventRepository extends ReactiveCrudRepository<ProcessedEventEntity, String> {
}