FROM bellsoft/liberica-openjdk-alpine:17

ARG COMMIT_HASH
ARG VERSION

ARG USER=gematik
ARG GROUP=gematik
ARG USER_HOME=/home/$USER
ARG UID=12345
ARG GID=23456

LABEL de.gematik.vendor="gematik GmbH"
LABEL maintainer="alexander.kaiser@gematik.de"
LABEL de.gematik.app="E-Rezept CLI FHIR"
LABEL de.gematik.git-repo-name="https://gitlab.prod.ccs.gematik.solutions/git/erezept/fachdienst/erp-e2e"
LABEL de.gematik.commit-sha=$COMMIT_HASH
LABEL de.gematik.version=$VERSION


RUN apk update  \
    && apk upgrade  \
    && apk add --no-cache zsh alpine-zsh-config curl git man-db man-pages less less-doc musl-locales \
    && addgroup -g $GID $GROUP \
    && adduser -D -h $USER_HOME -G $GROUP -u $UID $USER

# install the erpf command
COPY target/*_completion.sh /etc/bash-completion.d/
COPY target/erp-cli-fhir.zip /tmp/erp-cli-fhir.zip
RUN unzip /tmp/erp-cli-fhir.zip -d /tmp/erpf \
    && mv /tmp/erpf/bin/* /usr/bin \
    && mv /tmp/erpf/lib/* /usr/lib \
    && rm -rf /tmp/erpf \
    && rm /tmp/erp-cli-fhir.zip

# install manpages and bash-completion
COPY target/generated-docs/*.1 /usr/man/man1/
RUN echo 'for bcfile in /etc/bash-completion.d/* ; do . ${bcfile}; done' >> /etc/zsh/zshrc \
    && mandb \
    && export LC_CTYPE=en_US.UTF-8

# install oh-my-zsh as gematik user
USER gematik
RUN sh -c "$(wget -O- https://github.com/deluan/zsh-in-docker/releases/download/v1.1.5/zsh-in-docker.sh)" --\
            -x \
            -t robbyrussell \
            -p https://github.com/zsh-users/zsh-history-substring-search \
            -p https://github.com/zsh-users/zsh-autosuggestions \
            -p https://github.com/zsh-users/zsh-completions

WORKDIR /home/gematik
CMD ["/bin/zsh"]
