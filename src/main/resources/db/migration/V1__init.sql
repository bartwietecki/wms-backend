create table if not exists flyway_smoke_test (
                                                 id bigserial primary key,
                                                 created_at timestamptz not null default now()
    );
