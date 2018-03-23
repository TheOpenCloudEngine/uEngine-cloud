# Untag this, if you use docker hub to store your generated cloud package apps.
# docker login

# Untag this, if you need sudo
# sudo su

docker pull mesosphere/marathon-lb:v1.11.2
docker tag mesosphere/marathon-lb:v1.11.2 darkgodarkgo/marathon-lb:v1.11.2
docker push darkgodarkgo/marathon-lb:v1.11.2
docker rmi darkgodarkgo/marathon-lb:v1.11.2
docker rmi mesosphere/marathon-lb:v1.11.2


docker pull mysql:5.7
docker tag mysql:5.7 darkgodarkgo/mysql:5.7
docker push darkgodarkgo/mysql:5.7
docker rmi darkgodarkgo/mysql:5.7
docker rmi mysql:5.7


docker pull docker:latest
docker tag docker:latest darkgodarkgo/docker:latest
docker push darkgodarkgo/docker:latest
docker rmi darkgodarkgo/docker:latest
docker rmi docker:latest


docker pull node:latest
docker tag node:latest darkgodarkgo/node:latest
docker push darkgodarkgo/node:latest
docker rmi darkgodarkgo/node:latest
docker rmi node:latest


docker pull maven:3-jdk-8
docker tag maven:3-jdk-8 darkgodarkgo/maven:3-jdk-8
docker push darkgodarkgo/maven:3-jdk-8
docker rmi darkgodarkgo/maven:3-jdk-8
docker rmi maven:3-jdk-8


docker pull maven:3-jdk-7
docker tag maven:3-jdk-7 darkgodarkgo/maven:3-jdk-7
docker push darkgodarkgo/maven:3-jdk-7
docker rmi darkgodarkgo/maven:3-jdk-7
docker rmi maven:3-jdk-7


docker pull sppark/curl-jq:v1
docker tag sppark/curl-jq:v1 darkgodarkgo/curl-jq:v1
docker push darkgodarkgo/curl-jq:v1
docker rmi darkgodarkgo/curl-jq:v1
docker rmi sppark/curl-jq:v1


docker pull openjdk:8u111-jdk-alpine
docker tag openjdk:8u111-jdk-alpine darkgodarkgo/openjdk:8u111-jdk-alpine
docker push darkgodarkgo/openjdk:8u111-jdk-alpine
docker rmi darkgodarkgo/openjdk:8u111-jdk-alpine
docker rmi openjdk:8u111-jdk-alpine


docker pull tomcat:7.0.84-jre7
docker tag tomcat:7.0.84-jre7 darkgodarkgo/tomcat:7.0.84-jre7
docker push darkgodarkgo/tomcat:7.0.84-jre7
docker rmi darkgodarkgo/tomcat:7.0.84-jre7
docker rmi tomcat:7.0.84-jre7


docker pull webratio/nodejs-http-server
docker tag webratio/nodejs-http-server darkgodarkgo/nodejs-http-server
docker push darkgodarkgo/nodejs-http-server
docker rmi darkgodarkgo/nodejs-http-server
docker rmi webratio/nodejs-http-server


docker pull google/cadvisor:latest
docker tag google/cadvisor:latest darkgodarkgo/cadvisor:latest
docker push darkgodarkgo/cadvisor:latest
docker rmi darkgodarkgo/cadvisor:latest
docker rmi google/cadvisor:latest


docker pull sonatype/nexus:2.14.6-02
docker tag sonatype/nexus:2.14.6-02 darkgodarkgo/nexus:2.14.6-02
docker push darkgodarkgo/nexus:2.14.6-02
docker rmi darkgodarkgo/nexus:2.14.6-02
docker rmi sonatype/nexus:2.14.6-02


