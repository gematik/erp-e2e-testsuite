FROM bellsoft/liberica-openjdk-alpine:17

ARG COMMIT_HASH
ARG VERSION
ARG BUILD_DATE

ARG USER=primsys
ARG GROUP=primsys
ARG USER_HOME=/home/$USER
ARG UID=12345
ARG GID=23456

ENV TI_ENV=""
ENV VERSION=$VERSION
ENV COMMIT_HASH=$COMMIT_HASH
ENV BUILD_DATE=$BUILD_DATE
ENV HTTP_PROXY=""
ENV HTTPS_PROXY=""
ENV NO_PROXY="*"
ENV ERP_FHIR_PROFILE=""

LABEL de.gematik.vendor="gematik GmbH"
LABEL maintainer="alexander.kaiser@gematik.de"
LABEL de.gematik.app="E-Rezept PrimSys REST"
LABEL de.gematik.git-repo-name="https://gitlab.prod.ccs.gematik.solutions/git/erezept/fachdienst/erp-e2e"
LABEL de.gematik.commit-sha=$COMMIT_HASH
LABEL de.gematik.version=$VERSION


RUN apk update  \
    && apk upgrade  \
    && apk add curl \
    && addgroup -g $GID $GROUP \
    && adduser -D -h $USER_HOME -G $GROUP -u $UID $USER

USER primsys
WORKDIR /home/primsys

COPY --chown=primsys config/primsys/ /home/primsys/
COPY --chown=primsys target/rest-server-jar-with-dependencies.jar /home/primsys/app.jar

HEALTHCHECK CMD curl --fail http://localhost:9095/info || exit 1

EXPOSE 9095

# for -Djdk.tsl.namedGroups system property see https://github.com/bcgit/bc-java/issues/589
CMD java -Djdk.tls.namedGroups="secp256r1,secp384r1,ffdhe2048,ffdhe3072" -Djava.net.useSystemProxies=false -Derp.primsys.activeEnvironment=${TI_ENV} -jar app.jar /home/primsys/config.yaml