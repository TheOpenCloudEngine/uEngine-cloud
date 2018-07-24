# Untag this, if you use docker hub to store your generated cloud package apps.
# docker login

# Untag this, if you need sudo
# sudo su

docker pull sppark/uengine-lb:v1
docker tag sppark/uengine-lb:v1 sppark/uengine-lb:v1
docker push sppark/uengine-lb:v1
docker rmi sppark/uengine-lb:v1
docker rmi sppark/uengine-lb:v1


docker pull mysql:5.7
docker tag mysql:5.7 sppark/mysql:5.7
docker push sppark/mysql:5.7
docker rmi sppark/mysql:5.7
docker rmi mysql:5.7


docker pull docker:latest
docker tag docker:latest sppark/docker:latest
docker push sppark/docker:latest
docker rmi sppark/docker:latest
docker rmi docker:latest


docker pull node:latest
docker tag node:latest sppark/node:latest
docker push sppark/node:latest
docker rmi sppark/node:latest
docker rmi node:latest


docker pull maven:3-jdk-8
docker tag maven:3-jdk-8 sppark/maven:3-jdk-8
docker push sppark/maven:3-jdk-8
docker rmi sppark/maven:3-jdk-8
docker rmi maven:3-jdk-8


docker pull maven:3-jdk-7
docker tag maven:3-jdk-7 sppark/maven:3-jdk-7
docker push sppark/maven:3-jdk-7
docker rmi sppark/maven:3-jdk-7
docker rmi maven:3-jdk-7


docker pull sppark/curl-jq:v1
docker tag sppark/curl-jq:v1 sppark/curl-jq:v1
docker push sppark/curl-jq:v1
docker rmi sppark/curl-jq:v1
docker rmi sppark/curl-jq:v1


docker pull openjdk:8u111-jdk-alpine
docker tag openjdk:8u111-jdk-alpine sppark/openjdk:8u111-jdk-alpine
docker push sppark/openjdk:8u111-jdk-alpine
docker rmi sppark/openjdk:8u111-jdk-alpine
docker rmi openjdk:8u111-jdk-alpine


docker pull tomcat:7.0.84-jre7
docker tag tomcat:7.0.84-jre7 sppark/tomcat:7.0.84-jre7
docker push sppark/tomcat:7.0.84-jre7
docker rmi sppark/tomcat:7.0.84-jre7
docker rmi tomcat:7.0.84-jre7


docker pull webratio/nodejs-http-server
docker tag webratio/nodejs-http-server sppark/nodejs-http-server
docker push sppark/nodejs-http-server
docker rmi sppark/nodejs-http-server
docker rmi webratio/nodejs-http-server


docker pull google/cadvisor:latest
docker tag google/cadvisor:latest sppark/cadvisor:latest
docker push sppark/cadvisor:latest
docker rmi sppark/cadvisor:latest
docker rmi google/cadvisor:latest


docker pull sonatype/nexus:2.14.6-02
docker tag sonatype/nexus:2.14.6-02 sppark/nexus:2.14.6-02
docker push sppark/nexus:2.14.6-02
docker rmi sppark/nexus:2.14.6-02
docker rmi sonatype/nexus:2.14.6-02


docker pull sppark/kafka:v1
docker tag sppark/kafka:v1 sppark/kafka:v1
docker push sppark/kafka:v1
docker rmi sppark/kafka:v1
docker rmi sppark/kafka:v1


