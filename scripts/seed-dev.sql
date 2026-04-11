-- ============================================
-- WMS DEV SEED
-- For local development / demo purposes only
-- Assumes schema already exists (created by Flyway)
-- ============================================

BEGIN;

-- 1) Clear business data
TRUNCATE TABLE work_entries RESTART IDENTITY CASCADE;
TRUNCATE TABLE employees    RESTART IDENTITY CASCADE;
TRUNCATE TABLE users        RESTART IDENTITY CASCADE;
TRUNCATE TABLE departments  RESTART IDENTITY CASCADE;
TRUNCATE TABLE positions    RESTART IDENTITY CASCADE;

-- 2) Seed departments
INSERT INTO departments (name) VALUES
    ('Warehouse'),
    ('Human Resources'),
    ('Operations'),
    ('Sales'),
    ('Administration');

-- 3) Seed positions
INSERT INTO positions (name) VALUES
    ('Warehouse Worker'),
    ('HR Specialist'),
    ('Operations Coordinator'),
    ('Sales Specialist'),
    ('Team Leader'),
    ('Junior Operations Specialist'),
    ('Administrative Assistant');

-- 4) Seed users (LOCAL auth, no Keycloak yet)
INSERT INTO users (username, email, auth_provider, auth_subject, enabled) VALUES
    ('jan.kowalski',       'jan.kowalski@wms.local',       'LOCAL', 'local-1', true),
    ('anna.nowak',         'anna.nowak@wms.local',         'LOCAL', 'local-2', true),
    ('piotr.wisniewski',   'piotr.wisniewski@wms.local',   'LOCAL', 'local-3', true),
    ('karolina.wojcik',    'karolina.wojcik@wms.local',    'LOCAL', 'local-4', true),
    ('michal.kaminski',    'michal.kaminski@wms.local',    'LOCAL', 'local-5', true),
    ('zuzanna.lewandowska','zuzanna.lewandowska@wms.local','LOCAL', 'local-6', true),
    ('tomasz.zielinski',   'tomasz.zielinski@wms.local',   'LOCAL', 'local-7', false),
    ('natalia.szymanska',  'natalia.szymanska@wms.local',  'LOCAL', 'local-8', true);

-- 5) Seed employees
-- department_id: 1=Warehouse, 2=HR, 3=Operations, 4=Sales, 5=Administration
-- position_id:   1=Warehouse Worker, 2=HR Specialist, 3=Operations Coordinator,
--                4=Sales Specialist, 5=Team Leader, 6=Junior Operations Specialist,
--                7=Administrative Assistant
-- user_id matches the order inserted above
INSERT INTO employees (first_name, last_name, email, position, employment_type, active, user_id, department_id, position_id)
VALUES
    ('Jan',      'Kowalski',     'jan.kowalski@wms.local',       'Warehouse Worker',             'FULL_TIME', true,  1, 1, 1),
    ('Anna',     'Nowak',        'anna.nowak@wms.local',         'HR Specialist',                'FULL_TIME', true,  2, 2, 2),
    ('Piotr',    'Wiśniewski',   'piotr.wisniewski@wms.local',   'Operations Coordinator',       'FULL_TIME', true,  3, 3, 3),
    ('Karolina', 'Wójcik',       'karolina.wojcik@wms.local',    'Sales Specialist',             'B2B',       true,  4, 4, 4),
    ('Michał',   'Kamiński',     'michal.kaminski@wms.local',    'Team Leader',                  'FULL_TIME', true,  5, 1, 5),
    ('Zuzanna',  'Lewandowska',  'zuzanna.lewandowska@wms.local','Warehouse Worker',             'PART_TIME', true,  6, 1, 1),
    ('Tomasz',   'Zieliński',    'tomasz.zielinski@wms.local',   'Junior Operations Specialist', 'CONTRACT',  false, 7, 3, 6),
    ('Natalia',  'Szymańska',    'natalia.szymanska@wms.local',  'Administrative Assistant',     'FULL_TIME', true,  8, 5, 7);

-- 6) Seed work entries
-- employee_id 1 = Jan
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (1, DATE '2026-03-24', 480, 'Receiving inbound shipment and checking package labels.', 'APPROVED'),
    (1, DATE '2026-03-25', 450, 'Warehouse picking and stock verification.', 'APPROVED'),
    (1, DATE '2026-03-26', 420, 'Inventory count in sector B.', 'REJECTED'),
    (1, DATE '2026-03-27', 480, 'Preparing outbound pallets for courier pickup.', 'PENDING');

-- employee_id 2 = Anna
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (2, DATE '2026-03-24', 480, 'Prepared onboarding documents for new employees.', 'APPROVED'),
    (2, DATE '2026-03-25', 475, 'Updated employee records and contract metadata.', 'APPROVED'),
    (2, DATE '2026-03-27', 300, 'Scheduled recruitment interviews and internal meetings.', 'PENDING');

-- employee_id 3 = Piotr
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (3, DATE '2026-03-23', 480, 'Reviewed weekly warehouse workload plan.', 'APPROVED'),
    (3, DATE '2026-03-24', 460, 'Coordinated loading schedule with external carrier.', 'APPROVED'),
    (3, DATE '2026-03-25', 480, 'Resolved operational issue in dispatch workflow.', 'APPROVED'),
    (3, DATE '2026-03-28', 240, 'Prepared process improvement notes for supervisor.', 'PENDING');

-- employee_id 4 = Karolina
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (4, DATE '2026-03-24', 480, 'Followed up with leads and updated CRM notes.', 'APPROVED'),
    (4, DATE '2026-03-25', 430, 'Prepared offer summary for logistics client.', 'APPROVED'),
    (4, DATE '2026-03-26', 390, 'Client calls and post-meeting summary.', 'REJECTED'),
    (4, DATE '2026-03-29', 480, 'Prepared weekly sales pipeline review.', 'PENDING');

-- employee_id 5 = Michał
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (5, DATE '2026-03-24', 480, 'Managed shift planning and team task assignment.', 'APPROVED'),
    (5, DATE '2026-03-25', 480, 'Supervisor duties and escalation handling.', 'APPROVED'),
    (5, DATE '2026-03-26', 480, 'Performance check-in with warehouse team.', 'APPROVED'),
    (5, DATE '2026-03-30', 360, 'Prepared team weekly report.', 'PENDING');

-- employee_id 6 = Zuzanna
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (6, DATE '2026-03-24', 240, 'Picking support during peak hours.', 'APPROVED'),
    (6, DATE '2026-03-25', 240, 'Support in returns processing.', 'APPROVED'),
    (6, DATE '2026-03-27', 180, 'Shelf replenishment support.', 'PENDING');

-- employee_id 7 = Tomasz (inactive, old history)
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (7, DATE '2026-03-10', 480, 'Prepared operational notes before leave.', 'APPROVED'),
    (7, DATE '2026-03-11', 420, 'Internal documentation clean-up.', 'APPROVED');

-- employee_id 8 = Natalia
INSERT INTO work_entries (employee_id, work_date, minutes, description, status) VALUES
    (8, DATE '2026-03-24', 480, 'Invoice data verification and document archiving.', 'APPROVED'),
    (8, DATE '2026-03-25', 465, 'Prepared internal admin summaries.', 'APPROVED'),
    (8, DATE '2026-03-28', 240, 'Meeting notes and administrative support.', 'PENDING');

COMMIT;
