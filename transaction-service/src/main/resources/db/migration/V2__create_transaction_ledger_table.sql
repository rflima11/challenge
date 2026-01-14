CREATE TABLE tb_transaction_ledger
(
    id              UUID           NOT NULL,
    account_id      UUID           NOT NULL,
    operation       VARCHAR(50)    NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    balance_after   NUMERIC(19, 2) NOT NULL,
    idempotency_key VARCHAR(255)   NOT NULL,
    created_at      TIMESTAMP      NOT NULL,
    CONSTRAINT pk_tb_transaction_ledger PRIMARY KEY (id)
);

ALTER TABLE tb_transaction_ledger
    ADD CONSTRAINT uk_transaction_ledger_idempotency UNIQUE (idempotency_key);

ALTER TABLE tb_transaction_ledger
    ADD CONSTRAINT fk_transaction_ledger_account
        FOREIGN KEY (account_id) REFERENCES tb_account_balance (account_id);

CREATE INDEX idx_transaction_ledger_account_id ON tb_transaction_ledger (account_id);