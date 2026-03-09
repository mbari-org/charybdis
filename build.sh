#!/usr/bin/env bash

MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "$MY_DIR"

BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"`
VCS_REF=`git tag | sort -V | tail -1`

PROPS="src/main/resources/application.properties"

# --- Build 1: LOWER_CAMEL_CASE (default) ---
./mvnw clean package -DskipTests

docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t mbari/charybdis:${VCS_REF} \
    -t mbari/charybdis:latest \
    -f src/main/docker/Dockerfile.jvm \
    --push . && docker pull mbari/charybdis:latest

# --- Build 2: SNAKE_CASE ---
sed -i.bak \
    -e 's/^charybdis\.jackson\.property-naming-strategy=LOWER_CAMEL_CASE/charybdis.jackson.property-naming-strategy=SNAKE_CASE/' \
    -e 's/^quarkus\.jackson\.property-naming-strategy=LOWER_CAMEL_CASE/quarkus.jackson.property-naming-strategy=SNAKE_CASE/' \
    "$PROPS"

./mvnw clean package -DskipTests

docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t mbari/charybdis_sc:${VCS_REF} \
    -t mbari/charybdis_sc:latest \
    -f src/main/docker/Dockerfile.jvm \
    --push . && docker pull mbari/charybdis_sc:latest

# Restore original properties
mv "${PROPS}.bak" "$PROPS"
