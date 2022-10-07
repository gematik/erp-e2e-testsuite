FROM adoptopenjdk/maven-openjdk11:latest

LABEL de.gematik.vendor="gematik GmbH"
LABEL maintainer="johannes.plath@gematik.de"
LABEL de.gematik.app="E-Rezept Pharmacy-serviceProvider.ServiceProvider"
LABEL de.gematik.git-repo-name="https://gitlab.prod.ccs.gematik.solutions/git/erezept/fachdienst/erp-e2e.git"
LABEL de.gematik.commit-sha=$COMMIT_HASH
LABEL de.gematik.version=$VERSION

RUN groupadd -g 101 pharmacy
RUN useradd -m -s /bin/bash -g pharmacy -G root,sudo -u 1001 pharmacy
USER pharmacy
WORKDIR /home/pharmacy

COPY --chown=pharmacy target/pharmacy-serviceprovider-jar-with-dependencies.jar /home/pharmacy/app.jar

HEALTHCHECK CMD curl --fail http://localhost:9095/info || exit 1

EXPOSE 9095

CMD java -jar app.jar