-- Add new relationship columns to employees.
-- All nullable to avoid breaking existing data during transition.

alter table employees
    add column user_id       bigint references users(id),
    add column department_id bigint references departments(id),
    add column position_id   bigint references positions(id);

create index idx_employees_user_id       on employees(user_id);
create index idx_employees_department_id on employees(department_id);
create index idx_employees_position_id   on employees(position_id);

-- Note: the legacy `position` varchar column is intentionally kept for now.
-- It will be removed in a future migration once all data is migrated to position_id.
