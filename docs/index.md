# Charybdis

Charybdis is a query aggregation service for MBARI's [VARS](https://mbari-org.github.io/vars/) (Video Annotation Reference System). It federates queries across two backend services — **Annosaurus** (annotations) and **Vampire Squid** (media/video metadata) — and returns a combined response in a single `DataGroup` object:

```json
{ "annotations": [...], "media": [...] }
```

Service endpoints are resolved at startup via **Raziel**, the VARS configuration service.

The interactive API docs are available at `http://localhost:8080/q/swagger-ui` once the service is running.

---

| Page | Description |
|---|---|
| [Architecture](architecture.md) | How Charybdis fits into the VARS stack |
| [Deployment](deployment.md) | Run Charybdis with Docker |
| [Configuration](configuration.md) | All configuration properties and environment variable overrides |
| [Building](build.md) | Build the Docker image from source |
