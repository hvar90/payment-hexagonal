CREATE TABLE payments (
    id VARCHAR(255) PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

-- Índice clave para optimizar búsquedas por estado o consultas de conciliación
CREATE INDEX idx_payments_status ON payments(status);