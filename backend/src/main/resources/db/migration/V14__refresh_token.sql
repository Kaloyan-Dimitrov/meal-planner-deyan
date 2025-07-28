CREATE TABLE refresh_token (
                               id       BIGSERIAL     PRIMARY KEY,
                               user_id  BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               token    TEXT          NOT NULL UNIQUE,
                               expiry   TIMESTAMP     NOT NULL
);