CREATE TABLE employees (
    id uuid PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    email VARCHAR(60) NOT NULL,
    "position" VARCHAR(50) NOT NULL,
    supervisor uuid NULL,
    subordinates uuid [] NULL,
    created_at timestamp default now()
);
create index employees_supervisor_idx on employees(supervisor);
