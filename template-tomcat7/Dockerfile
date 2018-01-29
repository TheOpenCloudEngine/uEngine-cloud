FROM tomcat:7.0.84-jre7
VOLUME /tmp

ARG CI_PROJECT_NAME
ARG CI_COMMIT_SHA
ENV CI_PROJECT_NAME=$CI_PROJECT_NAME
ENV CI_COMMIT_SHA=$CI_COMMIT_SHA

ADD /target/*.war /usr/local/tomcat/webapps/ROOT.war
RUN chmod +x /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]