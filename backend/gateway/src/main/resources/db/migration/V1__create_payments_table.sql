CREATE TABLE payments (
    id VARCHAR(255) PRIMARY KEY,
    reference VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_payments_reference ON payments(reference);
CREATE INDEX idx_payments_status_created ON payments(status, created_at);