# Configuration

## Environment Variable Overrides

Any `application.properties` key can be overridden at runtime by setting an environment variable: uppercase all letters and replace `.` with `_`.

| Property | Environment variable |
|---|---|
| `raziel.service.url` | `RAZIEL_SERVICE_URL` |
| `annotation.service.timeout` | `ANNOTATION_SERVICE_TIMEOUT` |
| `annotation.service.pagesize` | `ANNOTATION_SERVICE_PAGESIZE` |
| `media.service.timeout` | `MEDIA_SERVICE_TIMEOUT` |
| `quarkus.http.port` | `QUARKUS_HTTP_PORT` |
| `charybdis.jackson.property-naming-strategy` | `CHARYBDIS_JACKSON_PROPERTY_NAMING_STRATEGY` |

Pass overrides to `docker run` with `-e`:

```shell
docker run -i --rm -p 8080:8080 \
  -e RAZIEL_SERVICE_URL=http://raziel:8085 \
  -e ANNOTATION_SERVICE_TIMEOUT=PT3M \
  mbari/charybdis
```

## Property Reference

| Property | Default | Description |
|---|---|---|
| `raziel.service.url` | `http://localhost:8085` | Raziel endpoint for service discovery |
| `annotation.service.timeout` | `PT2M` (2 minutes) | Timeout for Annosaurus requests |
| `annotation.service.pagesize` | `1000` | Page size for paginated Annosaurus fetches |
| `media.service.timeout` | `PT10S` (10 seconds) | Timeout for Vampire Squid requests |
| `quarkus.http.port` | `8080` | HTTP port |
| `charybdis.jackson.property-naming-strategy` | `LOWER_CAMEL_CASE` | JSON property naming: `LOWER_CAMEL_CASE` or `SNAKE_CASE` |

## JSON Naming Strategy

By default, Charybdis returns camelCase JSON property names (e.g., `videoReferenceUuid`, `recordedTimestamp`). To switch to snake_case (e.g., `video_reference_uuid`, `recorded_timestamp`):

```shell
-e CHARYBDIS_JACKSON_PROPERTY_NAMING_STRATEGY=SNAKE_CASE
```
