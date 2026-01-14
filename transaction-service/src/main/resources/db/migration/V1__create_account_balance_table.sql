CREATE TABLE tb_account_balance
(
    account_id      UUID           NOT NULL,
    balance         NUMERIC(19, 2) NOT NULL,
    status          VARCHAR(50)    NOT NULL,
    last_updated_at TIMESTAMP,
    CONSTRAINT pk_tb_account_balance PRIMARY KEY (account_id)
);