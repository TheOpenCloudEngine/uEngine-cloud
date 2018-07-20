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

$ rm -rf tomcat8/webapps/ROOT

$ chmod +x tomcat8/webapps/collector.war
$ chmod +x tomcat8/webapps/ROOT.war

# do as root
$ sudo su
$ sh tomcat8/bin/startup.sh
```

## Agent

유엔진 클라우드 플랫폼의 Jvm 기반 앱(`Springboot,Tomcat,Zuul,Iam`) 들은 도커 부팅시 호스트 머신에 설치된 네이버 핀포인트 에이전트 볼륨을 마운트하여, 
Jvm 트랜잭션 이벤트들을 핀포인트 콜렉터로 전송하게 됩니다.

**Agent-Architecture**

![](image/pinpoint-2.png)

### Copy agent to all nodes

`config.yml` 파일을 다음과 같이 수정합니다. `collector.ip` 에 pinpoint 가 설치된 ip 주소를 넣고, `web` 에는 pinpoint 에 외부에서 접속 가능한 주소를 기입합니다.

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

**설정 파일 빌드**

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

**인스톨 폴더에 agent 압축풀기**

```
$ mkdir install/pinpoint-agent
$ cd install/pinpoint-agent
$ cp ~/pinpoint-agent-1.7.1.tar.gz ./
$ tar xvf pinpoint-agent-1.7.1.tar.gz
$ rm pinpoint-agent-1.7.1.tar.gz
```

**Setting pinpoint.config**

`pinpoint.config` 파일의 `profiler.collector.ip` 의 아이피를 pinpoint 설치 주소로 변경합니다.

```
###########################################################
# Collector server                                        #
###########################################################
profiler.collector.ip=172.31.1.228

```

몇변의 트랜잭션마다 분석을 실행 할 것인지 샘플링 비율을 결정합니다. 예를 들면 `1` 은 `100%` , `20` 은 `5%` 입니다.  

```
# 1 out of n transactions will be sampled where n is the rate. (20: 5%)
profiler.sampling.rate=1
```

**publish all nodes**

수정한 에이전트 파일을 모든 서버에 배포합니다.

```
$ cd install
$ ansible-playbook ansible-pinpoint-agent.yml
```

### How does it work with apps

아래 예제는 클라우드 플랫폼에서 Jvm 계열 앱 생성시 기본 `Dockerfile` 의 내용입니다. `launcher.sh` 파일을 임시생성하고, 만약 클라우드 플랫폼으로부터 
`AGENT_USE` 여부가 확인된다면 Jvm 부팅시 에이전트를 구동시키도록 하는 내용을 포함하고 있습니다.

Jvm 기반의 사용자 템플릿 프로젝트를 제작할 때 아래 예제를 참조하여 구성할 수 있도록 합니다.

```
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
```








