CREATE TABLE links (
    id          BIGSERIAL PRIMARY KEY,
    url         VARCHAR(2048) NOT NULL,
    titel       VARCHAR(500),
    channel     VARCHAR(255),
    erstellt_am TIMESTAMP
);
