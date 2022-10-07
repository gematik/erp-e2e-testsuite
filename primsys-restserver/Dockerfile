FROM adoptopenjdk/maven-openjdk11:latest

LABEL de.gematik.vendor="gematik GmbH"
LABEL maintainer="alexander.kaiser@gematik.de"
LABEL de.gematik.app="E-Rezept PrimSys REST"
LABEL de.gematik.git-repo-name="https://build.top.local/source/git/erezept/fachdienst/erp-e2e.git"
LABEL de.gematik.commit-sha=$COMMIT_HASH
LABEL de.gematik.version=$VERSION


RUN groupadd -g 101 primsys
RUN useradd -m -s /bin/bash -g primsys -G root,sudo -u 1001 primsys
USER primsys
WORKDIR /home/primsys

COPY --chown=primsys config/primsys/ /home/primsys/
COPY --chown=primsys target/rest-server-jar-with-dependencies.jar /home/primsys/app.jar

HEALTHCHECK CMD curl --fail http://localhost:9095/info || exit 1

EXPOSE 9095

# choose IBM TU environment as default, alternatively provide another config at run with e.g. '-e CONF=docker_local_config.yaml'
ENV CONF "ibm_tu_config.yaml"

ENV HTTP_PROXY=""
ENV HTTPS_PROXY=""
ENV NO_PROXY="*"

CMD java -Djava.net.useSystemProxies=false -jar app.jar /home/primsys/${CONF}