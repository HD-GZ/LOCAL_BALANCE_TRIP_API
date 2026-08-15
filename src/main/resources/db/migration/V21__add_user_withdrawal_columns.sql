ALTER TABLE users
    ADD COLUMN withdrawn_at DATETIME(6) NULL,
    ADD COLUMN deleted_at DATETIME(6) NULL;
