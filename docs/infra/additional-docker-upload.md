# 필수 도커 업로드

이 튜토리얼을 진행하면, Docker hub 에서 필요한 도커 이미지들을 다운로드 받아 프라이빗 도커 레지스트리로 복사됩니다. 복사된 도커 이미지들은 클라우드 플랫폼에서 
제작된 앱이 Docker hub 를 경유하지 않고 실행될 수 있도록 하는데, 인터넷 격리환경에서 구동 될 수 있고, Docker image 다운로드에 소요되는 시간을 줄여주는 장점이 있습니다. 

이 섹션은 프라이빗 도커 레지스트리 서버가 필요합니다. 따라서 아래 목록의 설치 절차가 끝난 후 진행할 수 있도록 합니다.

- [유틸리티 설치](infra/install-util.md)
- [클러스터 설치](infra/install-cluster.md)
- [클러스터 계정 및 CLI](infra/install-cluster-user.md)
- [깃랩 && 도커 레지스트리 설치](infra/install-gitlab.md)

---
## 설정

필수 도커  `config.yml` 파일의 `` `mandatory-docker` 파트를 확인합니다. 

```
# Mandatory-docker part
mandatory-docker:
  - sppark/uengine-lb:v1
  - mysql:5.7
  - docker:latest
  - node:latest
  - maven:3-jdk-8
  - maven:3-jdk-7
  - sppark/curl-jq:v1
  - openjdk:8u111-jdk-alpine
  - tomcat:7.0.84-jre7
  - webratio/nodejs-http-server
  - google/cadvisor:latest
  - sonatype/nexus:2.14.6-02
  - sppark/kafka:v1
```


---
## 업로드

`uengine-resource` 에서 maven 을 통해 도커 업로드 스크립트를 생성합니다.

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

`install/mandatory-docker-upload.sh` 파일이 생성된 것을 확인합니다.

```
# Untag this, if you use docker hub to store your generated cloud package apps.
# docker login

# Untag this, if you need sudo
# sudo su

.
docker pull mysql:5.7
docker tag mysql:5.7 sppark/mysql:5.7
docker push sppark/mysql:5.7
docker rmi sppark/mysql:5.7
docker rmi mysql:5.7
.
.
.
```

생성된 스크립트를 실행하면, 수분가량 소요되며 private registry 로 도커 이미지를 업로드 하게 됩니다.

```
$ sudo sh install/mandatory-docker-upload.sh
```







