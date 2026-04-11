create table departments (
    id         bigserial    primary key,
    name       varchar(100) not null unique,
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);

create table positions (
    id         bigserial    primary key,
    name       varchar(100) not null unique,
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);
