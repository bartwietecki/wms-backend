create table leave_requests (
    id          bigserial    primary key,
    employee_id bigint       not null references employees(id),
    type        varchar(20)  not null,
    start_date  date         not null,
    end_date    date         not null,
    status      varchar(20)  not null default 'PENDING',
    reason      varchar(500),
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now(),
    constraint chk_leave_requests_dates check (start_date <= end_date)
);

create index idx_leave_requests_employee_id on leave_requests(employee_id);
create index idx_leave_requests_status      on leave_requests(status);
create index idx_leave_requests_start_date  on leave_requests(start_date);
