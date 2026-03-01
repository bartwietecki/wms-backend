create table work_entries (
                              id bigserial primary key,
                              employee_id bigint not null references employees(id),
                              work_date date not null,
                              minutes int not null,
                              description varchar(500),
                              status varchar(20) not null default 'PENDING',
                              created_at timestamptz not null default now(),
                              updated_at timestamptz not null default now(),
                              constraint chk_work_entries_minutes_positive check (minutes > 0)
);

create index idx_work_entries_employee_id on work_entries(employee_id);
create index idx_work_entries_work_date on work_entries(work_date);
create index idx_work_entries_employee_work_date on work_entries(employee_id, work_date);