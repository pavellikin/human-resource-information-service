# Human Resource Information Service

Service is responsible for employee information storage, management, and visualization.
The service allows to submission employees performance reviews and check all previous reviews for an employee.

The service was built with the following high level assumptions:
1. It is possible to create an employee placeholder without a supervisor and subordinates.
2. For the new employee existence of supervisors and subordinates will be checked. The new employee position should be below the supervisor position.
3. The checks for the new employee creation work also for the employee update. Name, surname, and email are not possible to update for simplicity.
4. Employee delete doesn't change links between employees. The delete consequences should be fixed by the next update requests.
5. Employee delete doesn't remove employee-related performance reviews. Old performance reviews can be used for later analysis. GDPR-related logic can be implemented in the next iteration.
6. For the org tree navigation in the Top direction every step will add `supervisor` and `supervisor.subordinates` to the result.
7. Performance reviews are implemented with the assumption that they are:
    - spontaneous
    - can be edited in one day only
    - reviewed doesn't see the reviewer

Assumptions that were made during implementation can be found in the code comments.

# Technical documentation

## Deployment strategy
![Deployment strategy](documents/Deployment%20strategy.png)
- Several instances of human resource information services can process user requests
- All instances that process user requests deployed behind a load balancer to distribute the load
- Dedicated Flyway pod is responsible for applying DB migrations

## Storage
Postgres was chosen as a service DB for several reasons:
1. For the `/employees` and `/organization/org-chart` endpoints the main type of requests will be read requests.
   For the `/performance-reviews` endpoints the number of write and read requests would be almost the same (assumption - employee creates a review and updates it up to two times, employee reads review at least 2 times).
   Relational DB should handle read-heavy requests well.
2. Even for large organizations of 2 million employees the amount of data looks manageable for relational DB:
- with the assumption that the median row in the `employees` table is about 200 bytes the table will be around 400 Mb
- with the assumption that the median row in the `performance_reviews` table is about 550 rows the table will grow on 1 Gb per review cycle

The table structure looks the following:
```
+----------------------------+
|        employees            |
+----------------------------+
| id (uuid)                  | <--- Primary Key
| name (varchar(60))         |
| surname (varchar(60))      |
| email (varchar(130))       |
| position (varchar(50))     |
| supervisor (uuid)          |
| subordinates (uuid[])      |
| created_at (date)          |
+----------------------------+

+----------------------------+
|  performance_reviews        |
+----------------------------+
| reviewee (uuid)            |
| reviewer (uuid)            |
| comment (varchar(2000))    |
| performance_score (smallint)|
| soft_skills_score (smallint)|
| independence_score (smallint)|
| aspiration_for_growth_score(smallint)|
| created_at (date)          | <--- Composite Primary Key (reviewee, created_at)
+----------------------------+
```

## Metrics
The service exposes the`/metrics` endpoint with HTTP and DB pool metrics.
Alert configuration is out of the scope of the repository. However, it is recommended to configure the following alerts in the alert manager:
1. API latency is greater than 1000 ms
2. API error rate is bigger than 10%

## Health checks
Health checks are exposed via `/health` endpoint. The endpoint checks the API and DB parts.

## Requirements
- Java 21
- Gradle 8.10+
- Docker
- Docker compose

## Build
`./gradlew clean build`

## Format
`./gradlew ktlintFormat`

## Generate migration scripts
1. Run the docker compose with a command `docker compose up postgres flyway`
2. Run the following gradle command `./gradlew generateMigrationScripts`

## Run application
1. Make sure the application was built - `./gradlew build`
2. Run the docker compose in the repository root directory - `docker compose up`
3. After the docker compose is started you can try the API using the following [Swagger UI](http://127.0.0.1:8080/openapi)
4. The DB populated with data from `V2__add_employees.sql` and `V4__add_performance_reviews.sql` scripts.

# Performance
## Employee org chart
For the 2 million rows in the employees table (the rows were generated with the following SQL)
```sql
INSERT INTO employees (id, name, surname, email, position, supervisor, created_at)
SELECT
    gen_random_uuid(),
    'Employee' || gs.id,
    'Surname' || gs.id,
    'employee' || gs.id || '@mycompany.com',
    CASE
        WHEN gs.id % 100000 = 0 THEN 'CEO'
        WHEN gs.id % 50000 = 0 THEN 'CTO'
        WHEN gs.id % 10000 = 0 THEN 'CPO'
        WHEN gs.id % 5000 = 0 THEN 'EngineeringManager'
        WHEN gs.id % 1000 = 0 THEN 'ProductManager'
        ELSE 'SoftwareEngineer'
    END,
    CASE
        WHEN gs.id % 100000 = 0 THEN NULL::uuid
        ELSE (SELECT id FROM employees ORDER BY random() LIMIT 1)::uuid
    END,
    NOW()::date
FROM generate_series(1, 2000000) AS gs(id);
```
the query plan to extract an employee look the following:
```sql
explain(analyze, buffers)
select * from
employees as current
left join employees as above on above.id = current.supervisor
left join employees as below on below.supervisor = current.id
where current.id = '00000000-0000-0000-0000-000000000006';

Nested Loop Left Join  (cost=0.85..80437.53 rows=1999895 width=426) (actual time=0.045..463.903 rows=1999982 loops=1)
  Buffers: shared hit=6788 read=28643
  ->  Nested Loop Left Join  (cost=0.85..16.89 rows=1 width=284) (actual time=0.032..0.035 rows=1 loops=1)
        Buffers: shared hit=8
        ->  Index Scan using employees_pkey on employees current  (cost=0.43..8.45 rows=1 width=142) (actual time=0.011..0.013 rows=1 loops=1)
              Index Cond: (id = '00000000-0000-0000-0000-000000000006'::uuid)
              Buffers: shared hit=4
        ->  Index Scan using employees_pkey on employees above  (cost=0.43..8.45 rows=1 width=142) (actual time=0.016..0.016 rows=1 loops=1)
              Index Cond: (id = current.supervisor)
              Buffers: shared hit=4
  ->  Seq Scan on employees below  (cost=0.00..60421.69 rows=1999895 width=142) (actual time=0.011..230.031 rows=1999982 loops=1)
        Filter: (supervisor = '00000000-0000-0000-0000-000000000006'::uuid)
        Rows Removed by Filter: 30
        Buffers: shared hit=6780 read=28643
Planning:
  Buffers: shared hit=10
Planning Time: 1.571 ms
Execution Time: 524.559 ms
```
