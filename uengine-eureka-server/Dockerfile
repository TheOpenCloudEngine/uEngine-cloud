FROM openjdk:8u111-jdk-alpine
VOLUME /tmp
ADD target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-Xmx256M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=docker"]