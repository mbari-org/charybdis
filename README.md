# charybdis

Charybdis is a query aggregation service for MBARI's [VARS](https://mbari-org.github.io/vars/) (Video Annotation Reference System). It federates queries across two backend services — **Annosaurus** (annotations) and **Vampire Squid** (media/video metadata) — and returns combined results in a single response. Service endpoints are resolved at startup via **Raziel**, the VARS configuration service.

---

## User Documentation

### Prerequisites

Charybdis requires a running VARS stack:
- **Raziel** — configuration/service-discovery service
- **Annosaurus** — annotation service
- **Vampire Squid** — media service

### Running with Docker

```shell
docker run -i --rm -p 8080:8080 \
  -e RAZIEL_SERVICE_URL=http://your-raziel-host:8085 \
  mbari/charybdis
```

Any `application.properties` key can be overridden at runtime by converting it to an environment variable: uppercase all letters and replace `.` with `_`. For example:

| Property | Environment variable |
|---|---|
| `raziel.service.url` | `RAZIEL_SERVICE_URL` |
| `annotation.service.timeout` | `ANNOTATION_SERVICE_TIMEOUT` |
| `quarkus.http.port` | `QUARKUS_HTTP_PORT` |

### API

Interactive API docs (Swagger UI) are available at `http://localhost:8080/q/swagger-ui`.

#### `GET /query/concept/{concept}`

Returns all annotations for a given concept name along with their associated media records.

Query parameters: `limit` (default 10000, max 10000), `offset` (default 0)

Response: `DataGroup` — `{ annotations: [...], media: [...] }`

#### `GET /query/dive/{videoSequenceName}`

Returns annotations and media for all video references within a named dive (video sequence).

Query parameters: `limit` (default 10000, max 10000), `offset` (default 0)

Response: `DataGroup` — `{ annotations: [...], media: [...] }`

#### `GET /query/file/{videoFileName}`

Returns annotations and media for a specific video file name.

Query parameters: `limit` (default 10000, max 10000), `offset` (default 0)

Response: `DataGroup` — `{ annotations: [...], media: [...] }`

#### `GET /count/dive/{videoSequenceName}`

Returns the total annotation count and a per-video-reference breakdown for a dive.

Response: `CountByMedia` — `{ count: <total>, annotationCounts: [...] }`

### Health and Observability

| Endpoint | Description |
|---|---|
| `GET /q/health` | Liveness check with server info (version, JDK, memory) |
| `GET /q/metrics` | Prometheus-format metrics |
| `GET /q/swagger-ui` | Swagger UI |
| `GET /q/openapi` | Raw OpenAPI spec |

---

## Developer Documentation

### Requirements

- Java 21
- Maven (or use the included `./mvnw` wrapper)
- A running VARS stack (or mock endpoints) accessible via Raziel

### Running in dev mode

```shell
./mvnw compile quarkus:dev
```

Live reload is enabled. The Quarkus Dev UI is at `http://localhost:8080/q/dev`.

### Running tests

```shell
# Unit tests
./mvnw test

# Single test class
./mvnw test -Dtest=ClassName

# Integration tests
./mvnw verify -DskipITs=false
```

### Building for deployment

```shell
# Build the fast-jar (default)
./mvnw package

# Run the packaged app
java -jar target/quarkus-app/quarkus-run.jar

# Build and run as Docker image
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t mbari/charybdis .
docker run -i --rm -p 8080:8080 mbari/charybdis
```

### Configuration reference

All configuration is in `src/main/resources/application.properties`.

| Property | Default | Description |
|---|---|---|
| `raziel.service.url` | `http://localhost:8085` | Raziel endpoint for service discovery |
| `annotation.service.timeout` | `120` (s) | Timeout for Annosaurus requests |
| `annotation.service.pagesize` | `1000` | Page size for paginated Annosaurus fetches |
| `media.service.timeout` | `10` (s) | Timeout for Vampire Squid requests |
| `quarkus.http.port` | `8080` | HTTP port |

### Debugging HTTP traffic

To log full request/response bodies for Vampire Squid HTTP calls, set the log level for the SDK factory to `DEBUG`. The SDK automatically adds an OkHttp `HttpLoggingInterceptor` (at `BODY` level) when DEBUG is active:

```properties
quarkus.log.category."org.mbari.vars.vampiresquid.sdk.VampireSquidFactory".level=DEBUG
```

---

## Technical Notes

### Architecture

Charybdis acts as an aggregation layer and does not have its own database. All data comes from Annosaurus and Vampire Squid. On startup, `AnnosaurusProvider` and `VampireSquidProvider` query Raziel to discover the live service URLs and produce CDI `@ApplicationScoped` beans that are injected throughout the application.

### Pagination across multiple video references

The `DataGroupService.limitedRequest` method implements cross-media pagination. When querying by dive or filename, a dive may span many video files. The service first fetches annotation counts per video reference, computes cumulative offsets, then issues targeted `(limit, offset)` fetches to Annosaurus for only the video references that fall within the requested window — avoiding fetching all annotations just to slice them.

### Concept name encoding

Concept names passed to Annosaurus are URL-encoded (`URLEncoder` with `UTF-8`, spaces as `%20`) before being placed in path segments, since concept names may contain characters such as spaces, slashes, or parentheses.

### System.Logger bridging

The Kiota-generated SDKs use Java's `System.Logger` API. Quarkus uses JBoss LogManager as the JUL manager, which implements `java.lang.System$LoggerFinder` and automatically bridges `System.Logger` calls into Quarkus' unified logging output. No extra configuration is required for the bridge itself; only log levels need to be set.

### Application version

`AppConfig.VERSION` is read from the MicroProfile config property `quarkus.application.version`, which Quarkus automatically populates from the Maven POM version at build time. This is exposed in the `/q/health` liveness response.
