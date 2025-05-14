create table files
(
    id                 bigserial primary key,
    content_type       varchar(255) not null,
    file_path          varchar(255) not null
        constraint uk_files_file_path unique,
    file_status        varchar(20) not null
        constraint files_file_status_check
            check ((file_status)::text = ANY ((ARRAY ['TEMP'::character varying, 'COMPLETE'::character varying])::text[])),
    original_file_name varchar(255) not null,
    created_at         timestamp(6) not null,
    created_by         varchar(20),
    updated_at         timestamp(6) not null,
    updated_by         varchar(20)
);

alter table files owner to pluxity;

create table revision_info
(
    revision_id bigserial primary key,
    revision_timestamp    bigint
);

alter table revision_info owner to pluxity;

create table roles
(
    id          bigserial primary key,
    description varchar(100),
    name        varchar(50) not null
        constraint uk_roles_name unique,
    created_at  timestamp(6) not null,
    created_by  varchar(20),
    updated_at  timestamp(6) not null,
    updated_by  varchar(20)
);

alter table roles owner to pluxity;

create table users
(
    id         bigserial primary key,
    code       varchar(50) not null,
    name       varchar(50)  not null,
    password   varchar(255) not null,
    username   varchar(20) not null
        constraint uk_users_username unique,
    created_at timestamp(6) not null,
    created_by varchar(20),
    updated_at timestamp(6) not null,
    updated_by varchar(20)
);

alter table users owner to pluxity;

create table user_role
(
    user_role_id bigserial primary key,
    role_id      bigint       not null
        constraint fk_user_role_role_id references roles,
    user_id      bigint       not null
        constraint fk_user_role_user_id references users,
    created_at   timestamp(6) not null,
    created_by   varchar(20),
    updated_at   timestamp(6) not null,
    updated_by   varchar(20)
);

alter table user_role owner to pluxity;

