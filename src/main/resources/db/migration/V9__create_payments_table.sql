CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    code UUID NOT NULL UNIQUE,
    expense_id BIGINT NOT NULL,
    group_member_id BIGINT NOT NULL,
    charged_value DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_payments_expense FOREIGN KEY (expense_id) REFERENCES expenses(id),
    CONSTRAINT fk_payments_group_member FOREIGN KEY (group_member_id) REFERENCES group_members(id)
);

