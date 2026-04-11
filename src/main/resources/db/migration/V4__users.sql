create table users (
    id          bigserial    primary key,
    username    varchar(100) not null unique,
    email       varchar(255) not null unique,
    auth_provider varchar(30) not null,
    auth_subject  varchar(255) unique,
    enabled     boolean      not null default true,
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now()
);

create index idx_users_username    on users(username);
create index idx_users_auth_subject on users(auth_subject);
