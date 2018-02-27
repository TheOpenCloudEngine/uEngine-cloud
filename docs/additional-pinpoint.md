# Install pinpoint

유엔진 클라우드 플랫폼은 Nave pinpoint 1.7.1 버젼과 호환됩니다. 별도의 CentOS VM 을 생성하여 설치를 진행하시길 바랍니다. 
이 VM 에는 핀포인트의 구성 요소인 Hbase , Web, Collector 가 설치됩니다. 

## 릴리즈 버전 다운로드

[https://github.com/naver/pinpoint/releases/tag/1.7.1](https://github.com/naver/pinpoint/releases/tag/1.7.1)

위의 핀포인트 Github 릴리즈 사이트로 이동하여, 다음 파일 목록을 내려받습니다.

- pinpoint-agent-1.7.1.tar.gz
- pinpoint-collector-1.7.1.war
- pinpoint-web-1.7.1.war
- Source code (zip)


## Hbase Start

내려받은 `Source code(zip)` 압축을 푼 후, Hbase 스크립트를 실행합니다.

**Download & Start**

```
$ quickstart/bin/start-hbase.sh
```

**Initialize Tables**

```
$ quickstart/bin/init-hbase.sh
```

**참고: Hbase 스키마 정보**

[https://github.com/naver/pinpoint/tree/master/hbase/scripts](https://github.com/naver/pinpoint/tree/master/hbase/scripts)


## Web, Collector Start

**Download Tomcat**

[Tomcat8 Download](http://mirror.navercorp.com/apache/tomcat/tomcat-8/v8.5.28/bin/apache-tomcat-8.5.28.tar.gz)

```
$ tar xvf apache-tomcat-8.5.28.tar.gz
$ mv apache-tomcat-8.5.28 tomcat8

# Edit web port 80

$ vi tomcat8/conf/server.xml

.
.
<Connector port="80" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
.
.               
```

**Start Web, Collector**

```
$ cp pinpoint-collector-1.7.1.war tomcat8/webapps/collector.war
$ cp pinpoint-web-1.7.1.war tomcat8/webapps/ROOT.war

$ chmod +x tomcat8/webapps/collector.war
$ chmod +x tomcat8/webapps/ROOT.war

$ sh tomcat8/bin/startup.sh
```

## Agent

유엔진 클라우드 플랫폼의 Jvm 기반 앱(`Springboot,Tomcat,Zuul,Iam`) 들은 도커 부팅시 호스트 머신에 설치된 네이버 핀포인트 에이전트 볼륨을 마운트하여, 
Jvm 트랜잭션 이벤트들을 핀포인트 콜렉터로 전송하게 됩니다.

**Agent-Architecture**

![](image/pinpoint-2.png)

### Copy agent to all nodes

 


플랫폼 도커 파일 변경

```
FROM openjdk:8u111-jdk-alpine
VOLUME /tmp
ADD *.jar app.jar
ARG CI_PROJECT_NAME
ARG CI_COMMIT_SHA
ENV CI_PROJECT_NAME=$CI_PROJECT_NAME
ENV CI_COMMIT_SHA=$CI_COMMIT_SHA
RUN echo 'JAVA_OPTS=""' > launcher.sh && \
    sed -i '$ a if [ "$AGENT_USE" == "true" ]; then' launcher.sh && \
    sed -i '$ a   JAVA_OPTS="-javaagent:$AGENT_PATH/pinpoint-bootstrap-1.7.1.jar"' launcher.sh && \
    sed -i '$ a   JAVA_OPTS="$JAVA_OPTS -Dpinpoint.agentId=${MESOS_TASK_ID##*-}"' launcher.sh && \
    sed -i '$ a   JAVA_OPTS="$JAVA_OPTS -Dpinpoint.applicationName=$APP_NAME-$PROFILE"' launcher.sh && \
    sed -i '$ a fi' launcher.sh && \
    sed -i '$ a java -Xmx400M -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app.jar' launcher.sh
EXPOSE 8080
ENTRYPOINT ["sh","launcher.sh"]
```

실행 (AGENT_USE 가 ture 면 핀포인트 연결, 없다면 일반 부팅이 된다.)

```
docker build -t iam-agent:v1 ./

export AGENT_PATH=/Users/uengine/docker/iam/pinpoint-agent

docker run -p 8080:8080 -v $AGENT_PATH:$AGENT_PATH -e SPRING_DATASOURCE_URL="jdbc:mysql://db.pas-mini.io:10002/uengine" -e SPRING_DATASOURCE_PASSWORD=my-secret-pw -e PROFILE=dev -e APP_NAME=iam -e MESOS_TASK_ID=front-end-dev.2c5b445e-0ba8-11e8-a4db-0aef186e3436 -e AGENT_PATH=$AGENT_PATH iam-agent:v1
```

다음 파라미터가 앱 실행시 추가되야 한다.
 - AGENT_USE:  마라톤 json 의 env 로 추가.
 - AGENT_PATH : 마라톤 json 의 볼륨으로 추가.
 - AGENT_PATH : 마라톤 json 의 env 로 추가.


config.yml pinpoint 관련 프로퍼티 추가.

```
# Pinpoint part
pinpoint:
  use: true
  agent-path: /pinpoint-agent
  web: http://pinpoint.pas-mini.io
  collector:
    ip: 172.31.1.228
    tcp-port: 9994
    stat-port: 9995
    span-port: 9996
```
 






