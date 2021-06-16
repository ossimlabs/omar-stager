#ARG BASE_IMAGE
#FROM ${BASE_IMAGE}
FROM nexus-docker-private-group.ossim.io/ossim-alpine-jdk11-runtime:1.5
ENV SPRING_CLOUD_CONFIG_ENABLED=true \
    SPRING_CLOUD_DISCOVERY_ENABLED=true
WORKDIR $HOME
EXPOSE 8080
COPY *.jar $HOME
CMD $HOME/run.sh
