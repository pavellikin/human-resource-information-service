-- Chat GPT generated
--                       Alice Bennett (CEO)
--                              |
--            ------------------------------------------------
--            |                                              |
--    Robert Carter (CTO)                           Laura Daniels (CPO)
--            |                                              |
--     ---------------------                          -------------------
--     |                   |                          |                 |
-- James Edwards     Emily Foster             Michael Green      Olivia Harris
--(Eng. Manager)    (Eng. Manager)            (Prod. Manager)    (Prod. Manager)
--     |                                              |
-- -------------                                 -------------
-- |           |                                 |           |
--Mark Irving  Sarah Jones               David King     Nora Lewis
--(Soft. Eng.) (Soft. Eng.)              (Soft. Eng.)  (Soft. Eng.)


INSERT INTO employees (id, name, surname, email, position, supervisor, subordinates, created_at) VALUES
('00000000-0000-0000-0000-000000000001'::uuid, 'Alice', 'Bennett', 'alice.bennett@mycompany.com', 'CEO', NULL, ARRAY['00000000-0000-0000-0000-000000000002'::uuid, '00000000-0000-0000-0000-000000000003'::uuid]::uuid[], NOW()),
('00000000-0000-0000-0000-000000000002'::uuid, 'Robert', 'Carter', 'robert.carter@mycompany.com', 'CTO', '00000000-0000-0000-0000-000000000001'::uuid, ARRAY['00000000-0000-0000-0000-000000000004'::uuid, '00000000-0000-0000-0000-000000000005'::uuid]::uuid[], NOW()),
('00000000-0000-0000-0000-000000000003'::uuid, 'Laura', 'Daniels', 'laura.daniels@mycompany.com', 'CPO', '00000000-0000-0000-0000-000000000001'::uuid, ARRAY['00000000-0000-0000-0000-000000000006'::uuid, '00000000-0000-0000-0000-000000000007'::uuid]::uuid[], NOW()),
('00000000-0000-0000-0000-000000000004'::uuid, 'James', 'Edwards', 'james.edwards@mycompany.com', 'EngineeringManager', '00000000-0000-0000-0000-000000000002'::uuid, ARRAY['00000000-0000-0000-0000-000000000008'::uuid, '00000000-0000-0000-0000-000000000009'::uuid]::uuid[], NOW()),
('00000000-0000-0000-0000-000000000005'::uuid, 'Emily', 'Foster', 'emily.foster@mycompany.com', 'EngineeringManager', '00000000-0000-0000-0000-000000000002'::uuid, NULL, NOW()),
('00000000-0000-0000-0000-000000000006'::uuid, 'Michael', 'Green', 'michael.green@mycompany.com', 'ProductManager', '00000000-0000-0000-0000-000000000003'::uuid, ARRAY['00000000-0000-0000-0000-000000000010'::uuid, '00000000-0000-0000-0000-000000000011'::uuid]::uuid[], NOW()),
('00000000-0000-0000-0000-000000000007'::uuid, 'Olivia', 'Harris', 'olivia.harris@mycompany.com', 'ProductManager', '00000000-0000-0000-0000-000000000003'::uuid, NULL, NOW()),
('00000000-0000-0000-0000-000000000008'::uuid, 'Mark', 'Irving', 'mark.irving@mycompany.com', 'SoftwareEngineer', '00000000-0000-0000-0000-000000000004'::uuid, NULL, NOW()),
('00000000-0000-0000-0000-000000000009'::uuid, 'Sarah', 'Jones', 'sarah.jones@mycompany.com', 'SoftwareEngineer', '00000000-0000-0000-0000-000000000004'::uuid, NULL, NOW()),
('00000000-0000-0000-0000-000000000010'::uuid, 'David', 'King', 'david.king@mycompany.com', 'SoftwareEngineer', '00000000-0000-0000-0000-000000000006'::uuid, NULL, NOW()),
('00000000-0000-0000-0000-000000000011'::uuid, 'Nora', 'Lewis', 'nora.lewis@mycompany.com', 'SoftwareEngineer', '00000000-0000-0000-0000-000000000006'::uuid, NULL, NOW())
on conflict do nothing;
