FROM openjdk:8u111-jdk-alpine
VOLUME /tmp
ADD /target/*.jar app.jar
ARG CI_PROJECT_NAME
ARG CI_COMMIT_SHA
ENV CI_PROJECT_NAME=$CI_PROJECT_NAME
ENV CI_COMMIT_SHA=$CI_COMMIT_SHA
RUN echo 'JAVA_OPTS=""' > launcher.sh && \
    sed -i '$ a if [ "$AGENT_USE" == "true" ]; then' launcher.sh && \
    sed -i '$ a   JAVA_OPTS="-javaagent:$AGENT_PATH/pinpoint-bootstrap-1.7.1.jar"' launcher.sh && \
    sed -i '$ a   JAVA_OPTS="$JAVA_OPTS -Dpinpoint.agentId=${MESOS_CONTAINER_NAME##*-}"' launcher.sh && \
    sed -i '$ a   JAVA_OPTS="$JAVA_OPTS -Dpinpoint.applicationName=$APP_NAME-$PROFILE"' launcher.sh && \
    sed -i '$ a fi' launcher.sh && \
    sed -i '$ a java -Xmx400M -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app.jar' launcher.sh
EXPOSE 8080
ENTRYPOINT ["sh","launcher.sh"]
