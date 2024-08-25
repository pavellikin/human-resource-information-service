CREATE TABLE employees (
    id uuid PRIMARY KEY,
    "name" VARCHAR(60) NOT NULL,
    surname VARCHAR(60) NOT NULL,
    email VARCHAR(130) NOT NULL,
    "position" VARCHAR(50) NOT NULL,
    supervisor uuid NULL,
    subordinates uuid [] NULL,
    created_at timestamp default now()
);
create index employees_supervisor_idx on employees(supervisor);
