# ApoloBackend

Backend application for the Apolo project (Hackathon Yuno) — a Spring Boot service that integrates AI features.

## Team

- Juan Sebastian Puentes Julio
- Daniel Patiño Mejia
- Tulio Riaño Sánchez
- Isaac Burgos

## Diagrams & Visuals (place images in docs/diagrams/)

Bring the architecture to life by adding diagrams and small animations. Store your images in `docs/diagrams/` and reference them with relative paths so they render correctly on GitHub.

Recommended diagram files (suggested names):

- `docs/diagrams/general-component-diagram.svg` — high-level system components and integrations (frontend, API, AI services, DB, external APIs).
- `docs/diagrams/specific-component-diagram.svg` — detailed backend components: controllers, services (AIService, GladiaService), repositories, mappers.
- `docs/diagrams/database-diagram.svg` or `.png` — database schema showing `Merchant`, `Interaction`, `RiskProfile`, and relationships.
- `docs/diagrams/class-diagram.svg` — important domain classes (entities, DTOs, mappers) and their relationships.

Example markdown to embed images (copy into this README where appropriate):

```markdown
<!-- Small animated banner (optional) -->
![Project banner animation](docs/diagrams/animated-banner.gif)

### General component diagram
![General component diagram](docs/diagrams/general-component-diagram.svg)

### Specific component diagram
![Specific component diagram](docs/diagrams/specific-component-diagram.svg)

### Database diagram
![Database diagram](docs/diagrams/database-diagram.svg)

### Class diagram
![Class diagram](docs/diagrams/class-diagram.svg)
```

Tips to make the README feel alive

- Use a lightweight animated banner (GIF or small WebP) at the top of the README for motion.
- Prefer SVG for diagrams (sharp and scalable). Use GIF/WebP for small looping animations.
- Keep assets small (ideally < 2–3 MB) so the README loads quickly.
- Add short captions or one-line descriptions beneath each image to guide the reader.

Tools & quick workflow

- Create diagrams with diagrams.net (draw.io), PlantUML (export to SVG), Lucidchart, or dbdiagram.io.
- Export to `svg` or `png`. For simple animations, export a short looping GIF or WebP.
- Place images under `docs/diagrams/`, commit them, and reference them using the markdown snippets above.

Accessibility

- Always include descriptive alt text for diagrams, for example: `![General component diagram showing frontend, backend, AI services, and MongoDB]`.

Next steps (I can help):

- Generate starter PlantUML files for each diagram type.
- Create a `docs/diagrams/README.md` with a checklist and export instructions.

Tell me which of those you'd like me to add and whether you prefer PlantUML or draw.io — I can generate starter files for you.

## Overview

This repository contains a Spring Boot backend (Java 17) that exposes REST endpoints and integrates with external AI services. It uses MongoDB for persistence and exposes API documentation via Swagger/OpenAPI.

Key technical details:
- Java: 17
- Framework: Spring Boot
- Build tool: Maven
- API docs: Swagger UI (springdoc)

## Prerequisites

- Java 17 or newer
- Maven 3.6+ (or newer)
- A MongoDB instance (local or cloud)

## Configuration

The application reads configuration from `src/main/resources/application.properties` by default. Sensitive values (API keys, DB credentials) should be provided through environment variables or JVM/system properties in production and should never be committed to source control.

Important properties used in the project (examples):
- `spring.data.mongodb.uri` — MongoDB connection URI
- `openai.api.key` — OpenAI API key (if used by services)
- `gladia.api.key` — Gladia API key

Examples to run without editing `application.properties` directly (zsh):

1) Using Maven (pass system properties):

```bash
export OPENAI_API_KEY="your-openai-key"
export GLADIA_API_KEY="your-gladia-key"
export MONGODB_URI="your-mongodb-uri"

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dopenai.api.key=$OPENAI_API_KEY -Dgladia.api.key=$GLADIA_API_KEY -Dspring.data.mongodb.uri=$MONGODB_URI"
```

2) Build and run the fat JAR (recommended for production):

```bash
mvn clean package -DskipTests
java -Dopenai.api.key="$OPENAI_API_KEY" -Dgladia.api.key="$GLADIA_API_KEY" -Dspring.data.mongodb.uri="$MONGODB_URI" -jar target/ApoloBackend-0.0.1-SNAPSHOT.jar
```

3) Run tests:

```bash
mvn test
```

## API documentation

When the application is running locally, the Swagger UI is available by default at:

http://localhost:8080/swagger-ui.html

## Common commands

- Build (with tests):

```bash
mvn clean package
```

- Build (skip tests):

```bash
mvn clean package -DskipTests
```

- Run from source (development):

```bash
mvn spring-boot:run
```

- Run tests:

```bash
mvn test
```

## Notes and security

- The repository should not contain API keys or production credentials. I noticed `application.properties` in this repo contains connection strings and API keys — please rotate these credentials and move them to a secure secret store.
- Prefer environment variables, secrets manager, or encrypted vaults in CI/CD for storing secrets.

## Troubleshooting

- If the application fails to connect to MongoDB, verify `spring.data.mongodb.uri` and network access (firewall, atlas IP whitelist).
- If external AI services fail, verify API keys and service availability.

## Contact

For questions about running or extending this project, contact any team member listed above.
