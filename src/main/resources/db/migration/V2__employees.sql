create table employees (
                           id bigserial primary key,
                           first_name varchar(100) not null,
                           last_name varchar(100) not null,
                           email varchar(255) not null unique,
                           position varchar(120),
                           employment_type varchar(30) not null,
                           active boolean not null default true,
                           created_at timestamptz not null default now(),
                           updated_at timestamptz not null default now()
);
