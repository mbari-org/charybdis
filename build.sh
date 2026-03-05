#!/usr/bin/env bash

MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "$MY_DIR"

BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"`
VCS_REF=`git tag | sort -V | tail -1`

./mvnw clean package -DskipTests

docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t mbari/charybdis:${VCS_REF} \
    -t mbari/charybdis:latest \
    -f Dockerfile \
    --push . && docker pull mbari/charybdis:latest


