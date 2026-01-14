CREATE TABLE tb_account (
                            id UUID NOT NULL,
                            status VARCHAR(50) NOT NULL,
                            owner_name VARCHAR(255) NOT NULL,
                            owner_document VARCHAR(20) NOT NULL,
                            owner_birth_date DATE NOT NULL,
                            phone_number VARCHAR(20),
                            email VARCHAR(255),
                            created_at TIMESTAMP,
                            CONSTRAINT pk_tb_account PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_account_document_active
    ON tb_account (owner_document)
    WHERE status IN ('ACTIVE', 'PENDING');