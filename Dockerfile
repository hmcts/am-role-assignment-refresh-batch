# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.6

# Application image

FROM hmctspublic.azurecr.io/base/java:21-distroless
USER hmcts
LABEL maintainer="https://github.com/hmcts/role-assignment-refresh-batch"

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/am-role-assignment-refresh-batch.jar /opt/app/

EXPOSE 5333
CMD [ "am-role-assignment-refresh-batch.jar" ]
