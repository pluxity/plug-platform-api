-- V5__add_line_schema.sql
CREATE TABLE line
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(50),
    color              VARCHAR(255),
    created_at         TIMESTAMP(6) NOT NULL,
    created_by         VARCHAR(20),
    updated_at         TIMESTAMP(6) NOT NULL,
    updated_by         VARCHAR(20)
);

ALTER TABLE station
    ADD COLUMN route VARCHAR(255);