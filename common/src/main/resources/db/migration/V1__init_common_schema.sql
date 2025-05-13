create table public.files
(
    id                 bigserial
        primary key,
    created_at         timestamp(6) not null,
    created_by         varchar(255),
    updated_at         timestamp(6) not null,
    updated_by         varchar(255),
    content_type       varchar(255) not null,
    file_path          varchar(255) not null
        constraint uk_t32k58330qnuj5ux3g144e8t8
            unique,
    file_status        varchar(255) not null
        constraint files_file_status_check
            check ((file_status)::text = ANY
        ((ARRAY ['TEMP'::character varying, 'COMPLETE'::character varying])::text[])),
    original_file_name varchar(255) not null
);

alter table public.files
    owner to pluxity;

create table public.roles
(
    id          bigserial
        primary key,
    created_at  timestamp(6) not null,
    created_by  varchar(255),
    updated_at  timestamp(6) not null,
    updated_by  varchar(255),
    description varchar(100),
    name        varchar(255) not null
        constraint uk_ofx66keruapi6vyqpv6f2or37
            unique
);

alter table public.roles
    owner to pluxity;

create table public.users
(
    id         bigserial
        primary key,
    created_at timestamp(6) not null,
    created_by varchar(255),
    updated_at timestamp(6) not null,
    updated_by varchar(255),
    code       varchar(255) not null,
    name       varchar(10)  not null,
    password   varchar(255) not null,
    username   varchar(255) not null
        constraint uk_r43af9ap4edm43mmtq01oddj6
            unique
);

alter table public.users
    owner to pluxity;

create table public.user_role
(
    user_role_id bigserial
        primary key,
    created_at   timestamp(6) not null,
    created_by   varchar(255),
    updated_at   timestamp(6) not null,
    updated_by   varchar(255),
    role_id      bigint       not null
        constraint fkt7e7djp752sqn6w22i6ocqy6q
            references public.roles,
    user_id      bigint       not null
        constraint fkj345gk1bovqvfame88rcx7yyx
            references public.users
);

alter table public.user_role
    owner to pluxity;

