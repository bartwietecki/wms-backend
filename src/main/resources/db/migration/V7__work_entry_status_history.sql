create table work_entry_status_history (
    id                  bigserial    primary key,
    work_entry_id       bigint       not null references work_entries(id),
    old_status          varchar(20)  not null,
    new_status          varchar(20)  not null,
    changed_at          timestamptz  not null default now(),
    changed_by_user_id  bigint       references users(id),
    comment             varchar(500)
);

create index idx_wesh_work_entry_id on work_entry_status_history(work_entry_id);
create index idx_wesh_changed_at    on work_entry_status_history(changed_at);
