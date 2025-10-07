CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    code UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

