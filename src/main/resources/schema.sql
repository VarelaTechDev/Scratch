CREATE TABLE IF NOT EXISTS app_user (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    username                VARCHAR(255) NOT NULL UNIQUE,
    recovery_token          BINARY(16),
    registration_start      TIMESTAMP,
    registration_add_start  TIMESTAMP,
    registration_add_token  BINARY(16)
    );

CREATE TABLE IF NOT EXISTS credentials (
    id              VARBINARY(128) NOT NULL,
    app_user_id     BIGINT NOT NULL,
    count           BIGINT NOT NULL,
    public_key_cose VARBINARY(255) NOT NULL,  -- Adjusted to fit within H2 limitations Originally was 500
    transports      VARCHAR(255),
    PRIMARY KEY(id, app_user_id),
    FOREIGN KEY (app_user_id) REFERENCES app_user(id) ON DELETE CASCADE
    );