CREATE TABLE links (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    url         VARCHAR(2048) NOT NULL,
    titel       VARCHAR(500),
    channel     VARCHAR(255),
    erstellt_am TIMESTAMP
);
