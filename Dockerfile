# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ARG APP_INSIGHTS_AGENT_VERSION=3.2.4

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless
USER hmcts
LABEL maintainer="https://github.com/hmcts/role-assignment-refresh-batch"

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/am-role-assignment-refresh-batch.jar /opt/app/

EXPOSE 5333
CMD [ "am-role-assignment-refresh-batch.jar" ]
