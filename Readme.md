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
