# music-metadata-service

ICE Music Metadata Service.

## Local Gradle development

Start PostgreSQL only with Docker Compose:

```bash
docker compose up -d postgres
```

Run the application from Gradle with the local Spring profile so it uses the Docker Compose database defaults:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The local profile defaults to:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/music_metadata`
- `SPRING_DATASOURCE_USERNAME=music`
- `SPRING_DATASOURCE_PASSWORD=music`

Those values can still be overridden with environment variables when needed.

## Full local stack with Docker Compose

Build and run the Spring Boot application and PostgreSQL together:

```bash
docker compose up --build
```

The Compose app service uses the `prod` Spring profile and supplies datasource settings through environment variables that point at the Compose PostgreSQL service. These are local development defaults only, not committed production secrets.

Stop the stack with:

```bash
docker compose down
```

## Production-like configuration

Use the `prod` Spring profile for production-like runs. Datasource settings must be supplied through environment variables; no production credentials are committed as fallbacks.

```bash
SPRING_PROFILES_ACTIVE=prod \
SPRING_DATASOURCE_URL=jdbc:postgresql://db.example.internal:5432/music_metadata \
SPRING_DATASOURCE_USERNAME=music_metadata_app \
SPRING_DATASOURCE_PASSWORD='<from-secret-manager>' \
java -jar build/libs/music-metadata-service-0.0.1-SNAPSHOT.jar
```

The Docker image accepts JVM options through `JAVA_OPTS`, for example:

```bash
JAVA_OPTS="-XX:MaxRAMPercentage=75" docker compose up --build app
```

## Useful URLs

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- Health: <http://localhost:8080/actuator/health>
- Prometheus metrics: <http://localhost:8080/actuator/prometheus>

Additional Actuator probe endpoints:

- <http://localhost:8080/actuator/health/liveness>
- <http://localhost:8080/actuator/health/readiness>
