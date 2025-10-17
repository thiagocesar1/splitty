CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    code UUID NOT NULL UNIQUE,
    value DECIMAL(10, 2) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP NOT NULL,
    category VARCHAR(20) NOT NULL,
    due_date DATE NOT NULL,
    group_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_expenses_group FOREIGN KEY (group_id) REFERENCES groups(id)
);
