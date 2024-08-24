# Human Resource Information Service

Service is responsible for employees information storage, management, and visualization.
The service allows to submit employees performance reviews and check all previous reviews for an employee.

## Requirements
- Java 21
- Gradle 8.10+
- Docker
- Docker compose

## Build
`./gradlew clean build`

## Format
`./gradlew ktlintFormat`

## Run application
1. Make sure the application was build - `./gradlew build`
2. Run the docker compose in the repository root directory - `docker compose up`
3. After the docker compose was started you can try the API using the following [Swagger UI](http://127.0.0.1:8080/openapi)
