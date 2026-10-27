package com.payment.gateway.infrastructure.adapter.outbound.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("processed_events")
public class ProcessedEventEntity {
    @Id
    @Column("event_id")
    private String eventId;

    @Column("payment_id")
    private String paymentId;

    @Column("event_type")
    private String eventType; // Nuevo

    @Column("status")
    private String status;

    @Column("processed_at")
    private LocalDateTime processedAt;

    public ProcessedEventEntity(String eventId, String paymentId, String eventType, String status, LocalDateTime processedAt) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.status = status;
        this.processedAt = processedAt;
    }

    public String getEventId() { return eventId; }
    public String getPaymentId() { return paymentId; }
    public String getEventType() { return eventType; }
    public String getStatus() { return status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}