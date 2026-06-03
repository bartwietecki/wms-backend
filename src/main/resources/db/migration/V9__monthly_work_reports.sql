create table monthly_work_reports (
    id            bigserial    primary key,
    employee_id   bigint       not null references employees(id),
    year          int          not null,
    month         int          not null,
    status        varchar(20)  not null,
    total_minutes int          not null,
    submitted_at  timestamptz,
    reviewed_at   timestamptz,
    admin_comment varchar(500),
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now(),
    constraint uq_mwr_employee_year_month unique (employee_id, year, month),
    constraint chk_mwr_month             check  (month between 1 and 12),
    constraint chk_mwr_total_minutes     check  (total_minutes >= 0)
);

create index idx_mwr_employee_id on monthly_work_reports(employee_id);
create index idx_mwr_status      on monthly_work_reports(status);
create index idx_mwr_year_month  on monthly_work_reports(year, month);
