create table public.asset
(
    id         bigserial
        primary key,
    created_at timestamp(6) not null,
    created_by varchar(255),
    updated_at timestamp(6) not null,
    updated_by varchar(255),
    file_id    bigint,
    name       varchar(255),
    type       varchar(255)
        constraint asset_type_check
            check ((type)::text = ANY
        ((ARRAY ['TWO_DIMENSION'::character varying, 'THREE_DIMENSION'::character varying])::text[]))
    );

alter table public.asset
    owner to pluxity;

create table public.facility_category
(
    id            bigserial
        primary key,
    created_at    timestamp(6) not null,
    created_by    varchar(255),
    updated_at    timestamp(6) not null,
    updated_by    varchar(255),
    name          varchar(255) not null,
    image_file_id bigint,
    parent_id     bigint
        constraint fk4k4duo1c9qcqsutbxyqecv90d
            references public.facility_category
);

alter table public.facility_category
    owner to pluxity;

create table public.facility
(
    facility_type     varchar(31)  not null,
    id                bigserial
        primary key,
    created_at        timestamp(6) not null,
    created_by        varchar(255),
    updated_at        timestamp(6) not null,
    updated_by        varchar(255),
    description       varchar(255),
    drawing_file_id   bigint,
    history_comment   varchar(255),
    name              varchar(255) not null,
    thumbnail_file_id bigint,
    category_id       bigint
        constraint fk9ln6ch6kdoh699hxymck6qlx9
            references public.facility_category
);

alter table public.facility
    owner to pluxity;

create table public.building
(
    id bigint not null
        primary key
        constraint fkkkkcamxj58nct01vymkxae0jt
            references public.facility
);

alter table public.building
    owner to pluxity;

create table public.feature
(
    id         bigserial
        primary key,
    created_at timestamp(6) not null,
    created_by varchar(255),
    updated_at timestamp(6) not null,
    updated_by varchar(255),
    position_x double precision,
    position_y double precision,
    position_z double precision,
    rotation_x double precision,
    rotation_y double precision,
    rotation_z double precision,
    scale_x    double precision,
    scale_y    double precision,
    scale_z    double precision
);

alter table public.feature
    owner to pluxity;

create table public.device
(
    id         bigserial
        primary key,
    created_at timestamp(6) not null,
    created_by varchar(255),
    updated_at timestamp(6) not null,
    updated_by varchar(255),
    asset_id   bigint
        constraint fkkos16ypbmtprl282n3sy3duiv
            references public.asset,
    feature_id bigint
        constraint fkfw17nl978ol6nasl7fvkvi2ee
            references public.feature
);

alter table public.device
    owner to pluxity;

create table public.floor
(
    id          bigserial
        primary key,
    group_id    integer      not null,
    name        varchar(255) not null,
    facility_id bigint
        constraint fka1umhafrx48hjvi3o8cgo87wy
            references public.facility
);

alter table public.floor
    owner to pluxity;

create table public.location
(
    id          bigserial
        primary key,
    altitude    double precision,
    latitude    double precision,
    longitude   double precision,
    facility_id bigint
        constraint fk5ln0fj4yfbhjm3i7lnjiytc41
            references public.facility
);

alter table public.location
    owner to pluxity;

create table public.panorama
(
    altitude  double precision,
    latitude  double precision,
    longitude double precision,
    id        bigint not null
        primary key
        constraint fk5v1vmco1670nc6hcjf9hxqwhi
            references public.facility
);

alter table public.panorama
    owner to pluxity;

create table public.revision_info
(
    revision_id bigserial
        primary key,
    revtstmp    bigint
);

alter table public.revision_info
    owner to pluxity;

create table public.facility_aud
(
    id              bigint      not null,
    rev             bigint      not null
        constraint fk9mhrpxtyc0ypa20j8ywvuej1s
            references public.revision_info,
    facility_type   varchar(31) not null,
    revtype         smallint,
    drawing_file_id bigint,
    primary key (rev, id)
);

alter table public.facility_aud
    owner to pluxity;

