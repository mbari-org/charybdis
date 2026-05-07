# Deployment

## Prerequisites

Charybdis requires a running VARS stack:

- **Raziel** — configuration/service-discovery service (default port 8085)
- **Annosaurus** — annotation service
- **Vampire Squid** — media service

Raziel must be reachable by Charybdis at startup. Annosaurus and Vampire Squid must be reachable via the URLs that Raziel provides.

## Running with Docker

```shell
docker run -i --rm -p 8080:8080 \
  -e RAZIEL_SERVICE_URL=http://your-raziel-host:8085 \
  mbari/charybdis
```

Replace `http://your-raziel-host:8085` with the URL where Raziel is running.

## Verifying

Once running, confirm Charybdis is healthy:

```shell
curl http://localhost:8080/q/health
```

A healthy response returns `{"status":"UP",...}`.

The interactive API docs are available at:

```
http://localhost:8080/q/swagger-ui
```

## Tuning

To adjust timeouts, page sizes, or JSON output format, see [Configuration](configuration.md).
