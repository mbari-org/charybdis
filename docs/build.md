# Building

## Prerequisites

- Java 21
- Maven 3.9+ — or use the included `./mvnw` wrapper (no separate Maven installation required)
- Docker

## Build the JAR

```shell
./mvnw package
```

This produces a fast-jar layout under `target/quarkus-app/`.

## Build the Docker Image

```shell
docker build -f src/main/docker/Dockerfile.jvm -t mbari/charybdis .
```

## Run Locally

```shell
docker run -i --rm -p 8080:8080 \
  -e RAZIEL_SERVICE_URL=http://your-raziel-host:8085 \
  mbari/charybdis
```

## Native Image (Optional)

To build a GraalVM native image without a local GraalVM installation, use the container build:

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

The resulting native binary is placed in `target/`.
