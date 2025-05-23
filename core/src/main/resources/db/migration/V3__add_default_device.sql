create table public.default_device
(
    icon_id     bigint,
    id          bigint primary key not null,
    code        character varying(255),
    description character varying(255),
    foreign key (id) references public.device (id)
        match simple on update no action on delete no action,
    foreign key (icon_id) references public.icon (id)
        match simple on update no action on delete no action
);

