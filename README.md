# music-metadata-service

ICE Music Metadata Service.

## Local development

Start PostgreSQL with Docker Compose:

```bash
docker compose up -d postgres
```

Run the application with the local Spring profile so it uses the Docker Compose database defaults:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The local profile defaults to:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/music_metadata`
- `SPRING_DATASOURCE_USERNAME=music`
- `SPRING_DATASOURCE_PASSWORD=music`

Those values can still be overridden with environment variables when needed.

## Production-like configuration

Use the `prod` Spring profile for production-like runs. Datasource settings must be supplied through environment variables; no production credentials are committed as fallbacks.

```bash
SPRING_PROFILES_ACTIVE=prod \
SPRING_DATASOURCE_URL=jdbc:postgresql://db.example.internal:5432/music_metadata \
SPRING_DATASOURCE_USERNAME=music_metadata_app \
SPRING_DATASOURCE_PASSWORD='<from-secret-manager>' \
java -jar build/libs/music-metadata-service-0.0.1-SNAPSHOT.jar
```

## Useful Actuator endpoints

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`
- `/actuator/prometheus`
