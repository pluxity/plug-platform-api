create table asset
(
    id         bigserial primary key,
    name       varchar(50) not null,
    file_id    bigint,
    created_at timestamp(6) not null,
    created_by varchar(20),
    updated_at timestamp(6) not null,
    updated_by varchar(20)
);

alter table asset owner to pluxity;

create table facility_category
(
    id            bigserial primary key,
    name          varchar(50) not null,
    image_file_id bigint,
    parent_id     bigint
        constraint fk_facility_category_parent_id references facility_category,
    created_at    timestamp(6) not null,
    created_by    varchar(20),
    updated_at    timestamp(6) not null,
    updated_by    varchar(20)
);

alter table facility_category owner to pluxity;

create table facility
(
    facility_type     varchar(50)  not null,
    id                bigserial primary key,
    description       varchar(255),
    drawing_file_id   bigint,
    history_comment   varchar(255),
    name              varchar(50) not null,
    thumbnail_file_id bigint,
    category_id       bigint
        constraint fk_facility_facility_category_id
            references facility_category,
    created_at        timestamp(6) not null,
    created_by        varchar(20),
    updated_at        timestamp(6) not null,
    updated_by        varchar(20)
);

alter table facility owner to pluxity;

create table building
(
    id bigint not null primary key constraint fk_building_facility_id references facility
);

alter table building owner to pluxity;

create table feature
(
    id         bigserial primary key,
    position_x double precision,
    position_y double precision,
    position_z double precision,
    rotation_x double precision,
    rotation_y double precision,
    rotation_z double precision,
    scale_x    double precision,
    scale_y    double precision,
    scale_z    double precision,
    created_at timestamp(6) not null,
    created_by varchar(20),
    updated_at timestamp(6) not null,
    updated_by varchar(20)
);

alter table feature owner to pluxity;

create table floor
(
    id          bigserial primary key,
    group_id    integer      not null,
    name        varchar(50) not null,
    facility_id bigint
        constraint fk_floor_facility_id
            references facility
);

alter table floor owner to pluxity;

create table icon
(
    id         bigserial primary key,
    file_id    bigint,
    name       varchar(50),
    created_at timestamp(6) not null,
    created_by varchar(20),
    updated_at timestamp(6) not null,
    updated_by varchar(20)
);

alter table icon owner to pluxity;

create table device_category
(
    id         bigserial primary key,
    name       varchar(50) not null,
    parent_id  bigint
        constraint fk_device_category_parent_id references device_category,
    icon_id    bigint
        constraint fk_device_category_icon_id references icon,
    created_at timestamp(6) not null,
    created_by varchar(20),
    updated_at timestamp(6) not null,
    updated_by varchar(20)
);

alter table device_category owner to pluxity;

create table device
(
    device_type varchar(50)  not null,
    id          bigserial primary key,
    name        varchar(50),
    asset_id    bigint
        constraint fk_device_asset_id
            references asset,
    category_id bigint
        constraint fk_device_category_id
            references device_category,
    facility_id bigint
        constraint fk_device_facility_id
            references facility,
    feature_id  bigint
        constraint uk_device_feature_id
            unique
        constraint fk_device_feature_id
            references feature,
    created_at  timestamp(6) not null,
    created_by  varchar(20),
    updated_at  timestamp(6) not null,
    updated_by  varchar(20)
);

alter table device owner to pluxity;

create table location
(
    id          bigserial primary key,
    altitude    double precision,
    latitude    double precision,
    longitude   double precision,
    facility_id bigint
        constraint fk_location_facility_id
            references facility,
    created_at  timestamp(6) not null,
    created_by  varchar(20),
    updated_at  timestamp(6) not null,
    updated_by  varchar(20)
);

alter table location owner to pluxity;

create table panorama
(
    altitude  double precision,
    latitude  double precision,
    longitude double precision,
    id        bigint not null
        primary key
        constraint fk_panorama_facility_id
            references facility
);

alter table panorama owner to pluxity;

create table facility_aud
(
    id              bigint      not null,
    rev             bigint      not null
        constraint fk_facility_aud_rev
            references revision_info,
    facility_type   varchar(31) not null,
    revtype         smallint,
    drawing_file_id bigint,
    primary key (rev, id)
);

alter table facility_aud owner to pluxity;

create table station
(
    id bigint not null
        primary key
        constraint fk_station_facility_id
            references facility
);

alter table station owner to pluxity;