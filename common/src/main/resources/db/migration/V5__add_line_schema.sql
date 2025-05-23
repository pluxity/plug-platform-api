CREATE TABLE line
(
    id                 bigserial primary key,
    name       VARCHAR(50),
    color      VARCHAR(255),
    created_at         timestamp(6) not null,
    created_by         varchar(20),
    updated_at         timestamp(6) not null,
    updated_by         varchar(20),
    CONSTRAINT pk_line PRIMARY KEY (id)
);