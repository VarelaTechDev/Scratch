CREATE TABLE user (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    PRIMARY KEY(user_id),
    UNIQUE(username)
);

CREATE TABLE credentials (
     id BIGINT NOT NULL AUTO_INCREMENT,
     user_id BIGINT NOT NULL,
     count BIGINT NOT NULL,
     credentialId VARBINARY(128) NOT NULL,
     public_key_cose VARBINARY(500) NOT NULL,
     transports VARCHAR(255),
     PRIMARY KEY(id),
     FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);