---
name: Charybdis Documentation Design
description: Design spec for operator and developer docs under docs/ using zensical/MkDocs Material
type: project
---

# Charybdis Documentation Design

## Audience

Primary: **operators** deploying and running Charybdis via Docker.
Secondary: **developers** building the Docker image from source.

API consumers rely on the Swagger UI at `/q/swagger-ui`; no standalone API reference page is included.

## Site Structure

Five markdown files under `docs/`:

| File | Title | Audience |
|---|---|---|
| `index.md` | Home | All |
| `architecture.md` | Architecture | Operators |
| `deployment.md` | Deployment | Operators |
| `configuration.md` | Configuration | Operators |
| `build.md` | Building | Developers |

### Updated `zensical.toml` nav

```toml
nav = [
    { "Home" = "index.md" },
    { "Architecture" = "architecture.md" },
    { "Deployment" = "deployment.md" },
    { "Configuration" = "configuration.md" },
    { "Building" = "build.md" },
]
```

## Page Designs

### `index.md` — Home

- One paragraph: Charybdis is a query aggregation service federating requests across Annosaurus (annotations) and Vampire Squid (media). Returns a combined `DataGroup` (`{ annotations: [...], media: [...] }`). Service endpoints resolved at startup via Raziel.
- Quick-reference table linking to all four other pages with one-line descriptions.
- Note: interactive API docs available at `/q/swagger-ui` once the service is running.

### `architecture.md` — Architecture

1. Prose overview of the four VARS services and their roles.
2. Mermaid diagram: startup-time Raziel lookup + request-time flow (client → Charybdis → Annosaurus + Vampire Squid).
3. Service roles table: Raziel (service discovery), Annosaurus (annotations), Vampire Squid (media/video metadata), Charybdis (aggregation).
4. Response shape: description of `DataGroup` — what `annotations` and `media` contain and how they're joined.

### `deployment.md` — Deployment

1. Prerequisites: Raziel, Annosaurus, and Vampire Squid must be running and reachable.
2. Docker run command with `-e RAZIEL_SERVICE_URL=...`.
3. Verify: `/q/health` health check, `/q/swagger-ui` for API exploration.
4. Pointer to Configuration page for tuning.

### `configuration.md` — Configuration

1. Env var override pattern: uppercase property key, replace `.` with `_`.
2. Full property reference table: `raziel.service.url`, `annotation.service.timeout`, `annotation.service.pagesize`, `media.service.timeout`, `quarkus.http.port`, `charybdis.jackson.property-naming-strategy`.
3. JSON naming strategy note: `LOWER_CAMEL_CASE` (default) and `SNAKE_CASE` both supported, switchable via `CHARYBDIS_JACKSON_PROPERTY_NAMING_STRATEGY`.

### `build.md` — Building

1. Prerequisites: Java 21, Maven / `./mvnw`, Docker.
2. Build the JAR: `./mvnw package`.
3. Build the Docker image: `docker build -f src/main/docker/Dockerfile.jvm -t mbari/charybdis .`
4. Run locally for verification: same `docker run` command as Deployment page.
5. Optional native image note: `./mvnw package -Dnative -Dquarkus.native.container-build=true`.

## Constraints

- Mermaid diagrams are supported via `pymdownx.superfences` — use them in architecture.md.
- No API reference page — Swagger UI is the canonical API reference.
- Docker-only deployment — no Maven/JAR run instructions in operator docs.
- Content derived from README.md and source code; README remains as-is.
