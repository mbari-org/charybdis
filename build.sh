#!/usr/bin/env bash

MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "$MY_DIR"

BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"`
VCS_REF=`git tag | sort -V | tail -1`

docker build --build-arg BUILD_DATE=$BUILD_DATE \
             --build-arg VCS_REF=$VCS_REF \
             -t portal/charybdis:${VCS_REF} \
             -t portal/charybdis:latest \
             -t portal.shore.mbari.org:5000/portal/charybdis \
             -f Dockerfile . && \
  docker push portal.shore.mbari.org:5000/portal/charybdis

ssh "brian@ione.shore.mbari.org" << 'ENDSSH'
  docker pull portal.shore.mbari.org:5000/portal/charybdis
  docker stop charybdis
  docker rm -f charybdis
  docker run -d --name=charybdis \
    -p 8300:8080 \
    -e ANNOTATION_SERVICE_URL="http://ione.mbari.org:8100/anno/v1" \
    -e ANNOTATION_SERVICE_TIMEOUT="PT10S" \
    -e MEDIA_SERVICE_URL="http://ione.mbari.org:8200/vam/v1" \
    -e MEDIA_SERVICE_TIMEOUT="PT10S" \
    --restart unless-stopped \
    portal.shore.mbari.org:5000/portal/charybdis
ENDSSH