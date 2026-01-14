CREATE TABLE tb_outbox (
                           id UUID NOT NULL,
                           topic VARCHAR(255) NOT NULL,
                           payload TEXT NOT NULL,
                           status VARCHAR(20) NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                           CONSTRAINT pk_tb_outbox PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_status_created
    ON tb_outbox (status, created_at);