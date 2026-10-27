CREATE TABLE processed_events (
    event_id VARCHAR(255) PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_processed_events_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE INDEX idx_processed_events_payment ON processed_events(payment_id);
CREATE INDEX idx_processed_events_type_date ON processed_events(event_type, processed_at);