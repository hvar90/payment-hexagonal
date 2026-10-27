CREATE TABLE processed_events (
    event_id VARCHAR(255) PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_processed_events_payment ON processed_events(payment_id);