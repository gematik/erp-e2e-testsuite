FROM nginx:1.23.3-alpine-slim

ARG COMMIT_HASH
ARG VERSION

LABEL de.gematik.vendor="gematik GmbH"
LABEL maintainer="alexander.kaiser@gematik.de"
LABEL de.gematik.app="E-Rezept PrimSys ngninx"
LABEL de.gematik.git-repo-name="https://gitlab.prod.ccs.gematik.solutions/git/erezept/fachdienst/erp-e2e"
LABEL de.gematik.commit-sha=$COMMIT_HASH
LABEL de.gematik.version=$VERSION

COPY nginx.conf /etc/nginx/conf.d/default.conf
#COPY api_keys.conf /etc/nginx/api_keys.conf
# COPY nginx.conf /usr/local/nginx/conf/nginx.conf


EXPOSE 8080 80 6095 443