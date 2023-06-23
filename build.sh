#!/usr/bin/env bash

MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "$MY_DIR"

BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"`
VCS_REF=`git tag | sort -V | tail -1`

# Github packages needs an access token. So move one into the docker context
cp "$HOME/.m2/settings.xml" "$MY_DIR/settings.xml"

# docker build --build-arg BUILD_DATE=$BUILD_DATE \
#              --build-arg VCS_REF=$VCS_REF \
#              -t mbari/charybdis:${VCS_REF} \
#              -t mbari/charybdis:latest \
#              -f Dockerfile . && \
#   docker push mbari/charybdis


`docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t mbari/charybdis:${VCS_REF} \
    -t mbari/charybdis:latest \
    -f Dockerfile \
    --push . `

rm "$MY_DIR/settings.xml"

ssh "$USER@ione.mbari.org" << 'ENDSSH'
  docker pull mbari/charybdis
  docker stop charybdis
  docker rm -f charybdis
  docker run -d --name=charybdis \
    -p 8300:8080 \
    -e ANNOTATION_SERVICE_URL="http://ione.mbari.org:8100/anno/v1" \
    -e ANNOTATION_SERVICE_TIMEOUT="PT20S" \
    -e MEDIA_SERVICE_URL="http://ione.mbari.org:8200/vam/v1" \
    -e MEDIA_SERVICE_TIMEOUT="PT10S" \
    --restart unless-stopped \
    mbari/charybdis
ENDSSH

ssh "$USER@quasar.shore.mbari.org" << 'ENDSSH'
  docker pull mbari/charybdis
  docker stop charybdis
  docker rm -f charybdis
  docker run -d --name=charybdis \
    -p 8300:8080 \
    -e ANNOTATION_SERVICE_URL="http://m3.shore.mbari.org/anno/v1" \
    -e ANNOTATION_SERVICE_TIMEOUT="PT20S" \
    -e MEDIA_SERVICE_URL="http://m3.shore.mbari.org/vam/v1" \
    -e MEDIA_SERVICE_TIMEOUT="PT10S" \
    --restart unless-stopped \
    mbari/charybdis
ENDSSH

n