# 클러스터 설치

---

## 설치파일 다운로드

하기 진행순서부터는 bootstrap 노드에서 진행되며, root 계정이 아닌 리눅스 사용자 계정으로 진행하시길 바랍니다 `ex) centos`

설치에 필요한 파일을 다운로드 받습니다.

```
$ cd

# 유엔진 클라우드 다운로드
$ git clone https://github.com/TheOpenCloudEngine/uEngine-cloud

# DCOS 인스톨 파일 다운로드
$ wget https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/dcos_generate_config.sh
```

---

## 설정 파일 편집

유엔진 클라우드는 앤서블 유틸리티를 사용해 설치자동화를 지원합니다. 

모든 설정이 `uEngine-cloud/uengine-resource/config.yml` 파일에서 이루어지는데, 이 설정들은 다음과 같은 작업을 자동화하는데 사용되어집니다. 

- 네트워크 환경 및 유틸리티 배포
- DC/OS 클러스터 설치
- 클라우드 플랫폼 빌드 및 배포


아래는, 설정 파일의 각 파트별 설명입니다.
 
---
 
### SSH and Resolve.conf part

| cluster_name                 | 클러스터 이름        |
|------------------------------|----------------------|
| ansible_user                 | ssh 접속 유저        |
| ansible_ssh_private_key_file | ssh 접속 키파일 위치 |
| ssh_port                     | ssh 포트             |
| resolvers                    | 네임서버 리스트      |
| dns_search                   | DNS 서치 서버        |

예제)

```
# SSH and Resolve.conf part
cluster_name: uEngine
ansible_user: centos
ansible_ssh_private_key_file: /home/centos/belugarKey.pem
ssh_port: 22
resolvers:
- 172.31.0.2
dns_search: ap-northeast-2.compute.internal
```

* 주의: ansible_ssh_private_key_file 에 명시된 키파일은 모든 서버에 ssh 접속이 가능한 키여야 합니다.
* 주의: 키파일의 퍼미션이 400 인지 확인하십시오.

여기서 resolvers 와 dns_search 는 다소 생소할 수 있지만, /etc/resolv.conf 조회를 통해 간단히 알아낼 수 있습니다.

```
# cat /etc/resolv.conf

search ap-northeast-2.compute.internal
nameserver 172.31.0.2
```

- nameserver 개수만큼 resolvers 에 넣습니다.
- search 가 있다면 dns_search 에 넣고, 없다면 공란으로 비워둡니다.

---

### Server list part

[서버 준비](./pre-server.md) 문서에서 준비했던 서버들의 아이피를 기입하는 파트입니다.

예)

| 역할 / 호스트네임 | IP 주소       | 퍼블릭 IP 주소 | 외부 포트바인딩     |
|-------------------|---------------|----------------|---------------------|
| bootstrap         | 172.31.8.143  |                |                     |
| master1           | 172.31.12.143 | 52.79.125.242  | 80                  |
| master2           | 172.31.4.125  |                |                     |
| master3           | 172.31.1.198  |                |                     |
| public-agent      | 172.31.5.136  | 52.79.51.79    | 80,443,9000 - 60000 |
| agent1            | 172.31.6.35   |                |                     |
| agent2            | 172.31.1.235  |                |                     |
| agent3            | 172.31.5.245  |                |                     |
| agent4            | 172.31.14.247 |                |                     |
| agent5            | 172.31.7.160  |                |                     |
| agent6            | 172.31.11.70  |                |                     |
| agent7            | 172.31.0.164  |                |                     |
| gitlab            | 172.31.15.249 | 52.78.60.43    | 80,5000             |
| ci                | 172.31.3.61   |                |                     |


설정예제)

```
# Server list part
server:
  private:
    bootstrap: 172.31.8.143
    gitlab: 172.31.15.249
    ci: 172.31.3.61
    public: 172.31.5.136

    master:
      master1: 172.31.12.143
      master2: 172.31.4.125
      master3: 172.31.1.198

    agent:
      agent1: 172.31.6.35
      agent2: 172.31.1.235
      agent3: 172.31.5.245
      agent4: 172.31.14.247
      agent5: 172.31.7.160
      agent6: 172.31.11.70
      agent7: 172.31.0.164

    add-agent:
    gracefully-remove-agent:
    uninstall:

  public:
    # Choice one of your master's public ip
    master: 52.79.125.242
    # Your public node's public ip
    public: 52.79.51.79
```

---

### DB part

유엔진 클라우드 패키지는 대부분의 데이터가 깃랩에 저장되고, 데이터베이스는 아주 적은 양의 데이터만 들어갑니다.
 
여기에는 사용자 인증정보와 앱 이력 정보가 들어가게 되는데, 데이터베이스가 중단되어도(심지어 데이터를 모두 분실해도)
 플랫폼 ui 로 일시적으로 접속을 못할 뿐, 운영중인 앱에 크리티컬한 영향은 주지 않습니다.

이러한 이유로 본 문서에서는 DC/OS 클러스터의 에이전트 노드 중 하나에 Mysql 도커를 실행하는 가이드를 제공합니다. (아래 설정 시 자동으로 수행됩니다.)

- db.ip : private agent 노드 중 하나를 선택하세요. 해당 노드의 /mysql/datadir 에 데이터가 저장됩니다.
- database : 데이터베이스 명
- password : 루트 패스워드

```
# DB part
db:
  ip: 172.31.6.35
  database: uengine
  password: my-secret-pw
```

---

### Domain part

[도메인 준비](./pre-domain.md) 문서에서 준비했던 도메인을 기입하는 파트입니다.

예)

| A_MASK        | 도메인      | 역할                   | 퍼블릭 아이피              |
|---------------|-------------|------------------------|----------------------------|
| gitlab        | pas-mini.io | 깃랩 / 도커 레지스트리 | 52.78.60.43(깃랩)       |
| config        | pas-mini.io | 클라우드 콘피그 서버   | 52.79.51.79(public-agent) |
| eureka-server | pas-mini.io | 유레카 서버            | 52.79.51.79(public-agent) |
| iam       | pas-mini.io | 사용자 인증 서버       | 52.79.51.79(public-agent) |
| db       | pas-mini.io | 데이터베이스   | 52.79.51.79(public-agent) |
| cloud-server  | pas-mini.io | 클라우드 플랫폼 서버   | 52.79.51.79(public-agent) |
| cloud         | pas-mini.io | 클라우드 플랫폼 UI     | 52.79.51.79(public-agent) |


설정예제)

```
host:
  registry:
    package: sppark
    private: gitlab.pas-mini.io:5000
    public: gitlab.pas-mini.io:5000
  db: db.pas-mini.io
  gitlab: gitlab.pas-mini.io
  iam: iam.pas-mini.io
  config: config.pas-mini.io
  eureka-server: eureka-server.pas-mini.io
  cloud-server: cloud-server.pas-mini.io
  cloud-ui: cloud.pas-mini.io
```

#### host.registry?

여기서 registry 란 도커 이미지가 저장되는 이미지 저장소입니다.

registry 서버는 깃랩서버와 동일한 곳에서 실행됨으로, registry.package 와 registry.public 모두 깃랩 도메인을 기입하면 됩니다. 
단, 5000 포트를 통해 서비스 될 것임으로 :5000 을 추가해주도록 합니다.

host.registry.package 는 유엔진 클라우드 패키지를 도커 파일을 저장하는 곳인데, 
개인 또는 회사의 DockerHub 가 있을 경우 DockerHub 의 아이디를 기입하도록 합니다.(추천)

만일 깃랩 서버에 설치된 registry 서버를 똑같이 사용하고가 하실 경우 마찬가지로 깃랩 도메인:5000 을 기입하도록 합니다.

---

### Security part

사용자 인증정보 서버에 관한 설정입니다.

```
iam:
  port: 80
  client-key: my-client-key
  client-secret: my-client-secret
  admin:
    username: admin
    password: admin
  access-token-lifetime: 7200
  mail:
    host: smtp.gmail.com
    username: sppark@uengine.org
    password: ********
    port: 587
    smtp-auth: true
    smtp-starttls-enable: true
    from-address: sppark@uengine.org
    from-name: uengine
```

- access-token-lifetime : 클라우드 플랫폼 로그인 유지시간입니다.
- admin.username : 인증서버 포탈 접속아이디
- admin.password : 인증서버 포탈 접속패스워드
- mail.**** : 이메일 서버 설정으로, 예제에서는 gmail 을 기준으로 작성되었습니다.

---

### Cloud package part

클라우드 패키지 파트는 [클러스터 설치](install-cluster.md) 와 [깃랩 && 도커 레지스트리 설치](install-gitlab.md) 
설치가 종료된 후, 클러스터 정보 및 깃랩 정보를 기입하는 곳입니다.

이 곳의 설정에 관해서는 [클라우드 패키지 빌드](install-package.md) 에서 진행하게 됩니다.

```
# Cloud package part
# Fill out those properties after install DC/OS cluster && Gitlab
# Then, you should re-command "mvn clean install exec:java package"
dcos:
  token:

gitlab:
  root:
    username:
    password:
    token:
  config-repo:
    projectId:
    deployment-path: /deployment
    template-path: /template
```

---

### Mandatory-docker part

이 파트는 클라우드 플랫폼에서 사용될 도커 이미지 목록입니다. 이 파트에 기록된 도커 이미지 리스트들은 [필수 도커 업로드](additional-docker-upload.md) 섹션을 
진행 할 때 Docker hub 에서 필요한 도커 이미지들을 다운로드 받아 프라이빗 도커 레지스트리로 복사됩니다.
 
```
# Mandatory-docker part
mandatory-docker:
  - mesosphere/marathon-lb:v1.11.2
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
```

| 이미지                           | 역할                                                                    |
|----------------------------------|-------------------------------------------------------------------------|
| - mesosphere/marathon-lb:v1.11.2 | Haproxy 로드밸런서 이미지                                               |
| - mysql:5.7                      | Mysql 도커 이미지                                                       |
| - docker:latest                  | CI 작업시 가상화 빌드 격리 공간을 위한 Docker in Docker 컨테이너 이미지 |
| - node:latest                    | CI 작업시 Npm 빌드를 위한 이미지                                        |
| - maven:3-jdk-8                  | CI 작업시 Maven 빌드를 위한 이미지                                      |
| - maven:3-jdk-7                  | CI 작업시 Maven 빌드를 위한 이미지                                      |
| - sppark/curl-jq:v1              | CI 작업시 클라우드 플랫폼과의 통신을 위한 이미지                        |
| - openjdk:8u111-jdk-alpine       | Springboot 계열 앱을 부팅시키기 위한 이미지                             |
| - tomcat:7.0.84-jre7             | Tomcat 계열 앱을 부팅시키기 위한 이미지                                 |
| - webratio/nodejs-http-server    | VueJs 를 비롯한 SPA 계열 앱을 부팅시키기 위한 이미지                    |
| - google/cadvisor:latest         | Docker metrics 모니터를 위한 이미지                                     |
| - sonatype/nexus:2.14.6-02       | Nexus 도커 이미지                                                       |


---

### Nexus part

Nexus 는 maven 에서 사용할 수 있는 가장 널리 사용되는 무료 repository 중의 하나로, maven,npm,yum 등 다양한 repository 를 사용할 수 있습니다.

Local 에 nexus 를 설치하게 되면, 외부로 부터 dependency 를 끌어 오는 수고를 덜고, local nexus 를 proxy (cache)로 사용함으로써 빠르게 라이브러리들을 끌어 올 수 도 있고, 
반대로 개발팀내에서 사용하는 공통 라이브러리들을 local nexus 에 배포해서 팀간에 공유할 수 도 있습니다. 또한 사용자 계정 지정을 통해서 repository 에 대한 접근 정책을 정의할 수 도 있습니다.


유엔진 클라우드에서 앱을 생성할 경우, 매우 작은 용량의 소스코드가 생성되는데, 이 앱은 CI 작업을 통해 필요한 디펜던시를 다운로드 받게 됩니다. 이 때 디펜던시를 다운받는 
용도로 넥서스 서버가 필요합니다.  

[Nexus 공식 사이트](http://www.sonatype.org/nexus/)


#### Use official nexus server

가장 쉬운 설정으로는 유엔진 official nexus 레파지토리를 사용하는 것입니다.

```
# Nexus part
nexus:
  ip: 
  username: 
  password: 
  mvn:
    public: http://nexus.pas-mini.io/nexus/content/groups/public/
    release: http://nexus.pas-mini.io/nexus/content/repositories/releases/
    snapshot: http://nexus.pas-mini.io/nexus/content/repositories/snapshots/
  npm:
    public: http://nexus.pas-mini.io/nexus/content/groups/npm/
    private: http://nexus.pas-mini.io/nexus/content/repositories/npm-private/
```  

#### Use own nexus server

데이터 센터 등 인터넷이 격리된 공간에서는 넥서스 서버를 직접 구축하여야 합니다. 넥서스 서버는 유엔진 클라우드 플랫폼 내부에 Docker 로 구축하거나,  
클라우드 플랫폼 외부에 설치할 수 도 있습니다.

[Nexus 구축](additional-nexus.md) 문서를 통해서 설치를 할 수 있도록 합니다.

문서 튜토리얼에 따라 플랫폼 내부에 Docker 로 구축할 경우 Agent 노드 중 하나의 아이피를 입력하셔야 합니다.

```
# Nexus part
nexus:
  ip: 192.168.0.11
  username: admin
  password: admin123
```

플랫폼 외부에 별도로 설치할 경우 해당하는 서버의 넥서스 url 를 기입하도록 합니다. 아래 값은 사설 넥서스 서버의 세팅에 따라 변경될 수 있습니다.

```
# Nexus part
nexus:
  ip: 
  username: 
  password: 
  mvn:
    public: http://my-nexus-server/nexus/content/groups/public/
    release: http://my-nexus-server/nexus/content/repositories/releases/
    snapshot: http://my-nexus-server/nexus/content/repositories/snapshots/
  npm:
    public: http://my-nexus-server/nexus/content/groups/npm/
    private: http://my-nexus-server/nexus/content/repositories/npm-private/
```  

---

### Pinpoint part(Optional)

Pinpoint 는 대규모 분산 시스템의 성능을 분석하고 문제를 진단, 처리하는 플랫폼이며, 2012년 7월에 개발을 시작해 2015년 1월 9일에 오픈소스로 공개한 네이버의 오픈소스입니다.

[GitHub의 Pinpoint 프로젝트](https://github.com/naver/pinpoint)

![](image/pinpoint-1.png)

Pinpoint 를 사용하게 되면 n계층 아키텍처를 추적해 대규모 분산 시스템의 성능을 분석하고 문제를 진단, 처리할 수 있습니다. 
대규모의 n계층 아키텍처를 추적할 수 있는 Pinpoint 는 다음과 같은 특징이 있습니다.

- 분산된 애플리케이션의 메시지를 추적할 수 있는 분산 트랜잭션 추적
- 애플리케이션 구성을 파악할 수 있는 애플리케이션 토폴로지 자동 발견
- 대규모 서버군을 지원할 수 있는 수평 확장성
- 코드 수준의 가시성을 제공해 문제 발생 지점과 병목 구간을 쉽게 발견
- bytecode instrumentation 기법으로 코드를 수정하지 않고 원하는 기능을 추가


유엔진 클라우드 플랫폼은 Java 계열의 앱 실행시 Pinpoint JVM agent 를 자동으로 앱에 mount 하여 분산 트랙잭션 모니터를 가능하게 합니다. 
이 과정에서 기존 앱의 소스코드를 전혀 수정할 필요가 없습니다.
Pinpoint 를 클라우드 플랫폼과 연동하기 위해서는 [Pinpoint](additional-pinpoint.md) 문서를 통해 설치를 진행하도록 합니다. 

아래는 설치 후 알맞은 설정의 예입니다.

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

---

### Elk part(Optional)

오늘날의 현대적이고 분산 된 컨테이너 형 애플리케이션 플랫폼의 복잡성을 감안할 때 개별 로그에 액세스하는 것은 비현실적이며 효과가 없습니다. 
ELK Stack (Elasticsearch, Logstash 및 Kibana) 은 이러한 장애물 중 일부를 극복하기위한 하나의 방법입니다. 

유엔진 클라우드 플랫폼은 ELK stack 과의 연동을 통해 다음의 기능을 제공합니다.

 - 앱별 통합 로그 검색
 - 앱별 Docker 메트릭스 정보 검색 및 시각화
 - 호스트 머신 메트릭스 정보 검색 및 시각화
 - 사용자 지정 태그에 의한 통합 로그 검색

또한, 적재된 로그 및 메트릭스 데이터는 API 를 통해 APM 서버나 데이터 분석 솔루션 등에 정보를 제공할 수 있습니다.

유엔진 클라우드 플랫폼은 Docker 를 통해 매우 빠르게 ELK 를 구축하고, 생성된 앱의 각종 정보를 저장합니다. 이 과정에서 기존 앱의 소스코드를 전혀 수정할 필요가 없습니다. 
[ELK](additional-elk.md) 문서를 통해 설치를 진행 한 후, 올바른 설정 예는 다음과 같습니다.
 
```
# Elk part
elk:
  elasticsearch:
    private: 172.31.11.218:9200
    web: http://13.125.185.128:9200
  kibana:
    private: 172.31.11.218:5601
    web: http://13.125.185.128:5601
    dashboard-id:
      app-log: uengine-app-log
      docker-metric: uengine-doker-metric
      system-metric: Metricbeat-system-overview
```

---

### Summary

전체 설정 파일의 리뷰입니다.

```
# SSH and Resolve.conf part
cluster_name: uEngine
ansible_user: centos
ansible_ssh_private_key_file: /home/centos/belugarKey.pem
ssh_port: 22
resolvers:
- 172.31.0.2
dns_search: ap-northeast-2.compute.internal

# Server list part
server:
  private:
    bootstrap: 172.31.8.143
    gitlab: 172.31.15.249
    ci: 172.31.3.61
    public: 172.31.5.136

    master:
      master1: 172.31.12.143
      master2: 172.31.4.125
      master3: 172.31.1.198

    agent:
      agent1: 172.31.6.35
      agent2: 172.31.1.235
      agent3: 172.31.5.245
      agent4: 172.31.14.247
      agent5: 172.31.7.160
      agent6: 172.31.11.70
      agent7: 172.31.0.164
      agent8: 172.31.10.202
      agent9: 172.31.14.197
      agent10: 172.31.13.131
      agent11: 172.31.1.166
      agent12: 172.31.8.5

    add-agent:
    gracefully-remove-agent:
    uninstall:

  public:
    # Choice one of your master's public ip
    master: 52.79.125.242
    # Your public node's public ip
    public: 52.79.51.79

# DB part
db:
  ip: 172.31.6.35
  database: uengine
  password: my-secret-pw

# Domain part
host:
  default: pas-mini.io
  registry:
    package: sppark
    private: gitlab.pas-mini.io:5000
    public: gitlab.pas-mini.io:5000
  db: db.pas-mini.io
  gitlab: gitlab.pas-mini.io
  iam: iam.pas-mini.io
  eureka-server: eureka-server.pas-mini.io
  cloud-server: cloud-server.pas-mini.io
  cloud-ui: cloud.pas-mini.io

# Security(IAM) part
iam:
  port: 80
  client-key: my-client-key
  client-secret: my-client-secret
  admin:
    username: admin
    password: admin
  access-token-lifetime: 7200
  mail:
    host: smtp.gmail.com
    username: "sppark@uengine.org"
    password: "!gosu23546"
    port: 587
    smtp-auth: true
    smtp-starttls-enable: true
    from-address: sppark@uengine.org
    from-name: uengine

# Mandatory-docker part
mandatory-docker:
  - mesosphere/marathon-lb:v1.11.2
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

# Cloud package part
# Fill out those properties after install DC/OS cluster && Gitlab
# Then, you should re-command "mvn clean install exec:java package"
dcos:
  token: 

gitlab:
  root:
    username: 
    password: 
    token: 
  config-repo:
    projectId: 
    deployment-path: /deployment
    template-path: /template

# Nexus part
nexus:
  ip: 172.31.10.202
  username: admin
  password: admin123
  mvn:
    public: http://nexus.pas-mini.io/nexus/content/groups/public/
    release: http://nexus.pas-mini.io/nexus/content/repositories/releases/
    snapshot: http://nexus.pas-mini.io/nexus/content/repositories/snapshots/
  npm:
    public: http://nexus.pas-mini.io/nexus/content/groups/npm/
    private: http://nexus.pas-mini.io/nexus/content/repositories/npm-private/

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

# Elk part
elk:
  elasticsearch:
    private: 172.31.11.218:9200
    web: http://13.125.185.128:9200
  kibana:
    private: 172.31.11.218:5601
    web: http://13.125.185.128:5601
    dashboard-id:
      app-log: uengine-app-log
      docker-metric: uengine-doker-metric
      system-metric: Metricbeat-system-overview
```

## 설정 파일 빌드

uEngine-cloud/uengine-resource/config.yml 을 모두 작성 후, 빌드를 실행합니다.

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

빌드가 성공적으로 진행되면, uEngine-cloud 폴더에 디렉토리들이 추가로 생성되어, 아래와 같은 모습이 됩니다.

```
.
├── LICENSE
├── README.md
├── cloud-config-repository
├── deploys
├── docs
├── install
├── pom.xml
├── template-iam
├── template-springboot
├── template-tomcat7
├── template-tomcat7.iml
├── template-vuejs
├── template-zuul
├── uengine-cloud-api
├── uengine-cloud-config
├── uengine-cloud-iam
├── uengine-cloud-server
├── uengine-cloud-ui
├── uengine-cloud-zuul
├── uengine-cloud.iml
├── uengine-eureka-server
└── uengine-resource
```

## DC/OS 클러스터 설치

### 유틸리티 및 도커 프로비져닝

```
$ cd install
$ sudo sh -c "cat ansible-hosts.yml > /etc/ansible/hosts"

$ ansible-playbook ansible-install.yml

PLAY [all] *********************************************************************************************************************************************

TASK [Gathering Facts] *********************************************************************************************************************************
ok: [172.31.3.61]
ok: [172.31.8.143]
ok: [172.31.1.235]
ok: [172.31.6.35]
.
.
```

* 팁: 단계별로 실행을 원할 경우 --step 옵션을 추가하도록 합니다.

```
$ ansible-playbook --step ansible-install.yml
```


### DC/OS 클러스터 인스톨

배포 패키지 생성

```
# copy your ssh key file to genconf folder
$ cp <your-private-key-file-path> ./genconf/ssh_key

$ mv ~/dcos_generate_config.sh ./
$ sudo bash dcos_generate_config.sh --genconf

====> EXECUTING CONFIGURATION GENERATION
Generating configuration files...
Package filename: packages/dcos-config/dcos-config--setup_5a900644a2b78900d7420b5d904c19a7e24d539b.tar.xz
Package filename: packages/dcos-metadata/dcos-metadata--setup_5a900644a2b78900d7420b5d904c19a7e24d539b.tar.xz
Generating Bash configuration files for DC/OS
```

preflight 는 사전 체크 단계입니다. 이 단계에서 모든 사항에 대해 pass 가 나오지 않는다면, [트러블 슈팅](trouble.md) 을 통해 해결하세요.

```
sudo bash dcos_generate_config.sh --preflight

====> EXECUTING_PREFLIGHT
====> START run_preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> STAGE preflight_cleanup
====> OUTPUT FOR run_preflight
.
.
```

deploy 는 설치 단계입니다. 수분 이상 소요될 수 있습니다.

```
sudo bash dcos_generate_config.sh --deploy
```

postflight 는 설치 후 확인 단계입니다. 이 단계에서 모든 DC/OS 컴포넌트의 헬스체크를 하게 되며, 만약 이 단계에서 수분간 동작을 멈추거나 Fail 이 떨어지게 된다면, 트러블 슈팅을 통해 이슈를 해결하셔야 합니다.

```
sudo bash dcos_generate_config.sh --postflight
```

### 클러스터 상태 점검

설치 수분 후, 정상적으로 설치가 되었다면 마스터 노드 중 한 곳에 접속하였을 때, 아래의 포트 리스트와 동일한 포트가 존재해야 합니다.

```
ssh -i <your-key-file> master1

(No info could be read for "-p": geteuid()=1000 but you should be root.)
Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name    
tcp        0      0 0.0.0.0:61420           0.0.0.0:*               LISTEN      -                   
tcp        0      0 198.51.100.3:63053      0.0.0.0:*               LISTEN      -                   
tcp        0      0 198.51.100.2:63053      0.0.0.0:*               LISTEN      -                   
tcp        0      0 198.51.100.1:63053      0.0.0.0:*               LISTEN      -                   
tcp        0      0 172.31.12.143:63053     0.0.0.0:*               LISTEN      -                   
tcp        0      0 172.17.0.1:63053        0.0.0.0:*               LISTEN      -                   
tcp        0      0 127.0.0.1:63053         0.0.0.0:*               LISTEN      -                   
tcp        0      0 127.0.0.1:15055         0.0.0.0:*               LISTEN      -                   
tcp        0      0 0.0.0.0:111             0.0.0.0:*               LISTEN      -                   
tcp        0      0 0.0.0.0:80              0.0.0.0:*               LISTEN      -                   
tcp        0      0 198.51.100.3:53         0.0.0.0:*               LISTEN      -                   
tcp        0      0 198.51.100.2:53         0.0.0.0:*               LISTEN      -                   
tcp        0      0 198.51.100.1:53         0.0.0.0:*               LISTEN      -                   
tcp        0      0 172.31.12.143:53        0.0.0.0:*               LISTEN      -                   
tcp        0      0 172.17.0.1:53           0.0.0.0:*               LISTEN      -                   
tcp        0      0 127.0.0.1:53            0.0.0.0:*               LISTEN      -                   
tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      -                   
tcp        0      0 127.0.0.1:25            0.0.0.0:*               LISTEN      -                   
tcp        0      0 172.31.12.143:5050      0.0.0.0:*               LISTEN      -                   
tcp        0      0 0.0.0.0:443             0.0.0.0:*               LISTEN      -                   
tcp        0      0 127.0.0.1:62080         0.0.0.0:*               LISTEN      -                   
tcp        0      0 172.31.12.143:15201     0.0.0.0:*               LISTEN      -                   
tcp        0      0 127.0.0.1:62053         0.0.0.0:*               LISTEN      -                   
tcp        0      0 0.0.0.0:62501           0.0.0.0:*               LISTEN      -                   
tcp        0      0 127.0.0.1:8101          0.0.0.0:*               LISTEN      -                   
tcp        0      0 0.0.0.0:62502           0.0.0.0:*               LISTEN      -                   
tcp        0      0 0.0.0.0:8008            0.0.0.0:*               LISTEN      -                   
tcp6       0      0 :::61420                :::*                    LISTEN      -                   
tcp6       0      0 :::111                  :::*                    LISTEN      -                   
tcp6       0      0 :::8080                 :::*                    LISTEN      -                   
tcp6       0      0 :::3888                 :::*                    LISTEN      -                   
tcp6       0      0 :::8181                 :::*                    LISTEN      -                   
tcp6       0      0 :::9942                 :::*                    LISTEN      -                   
tcp6       0      0 :::22                   :::*                    LISTEN      -                   
tcp6       0      0 :::41303                :::*                    LISTEN      -                   
tcp6       0      0 ::1:25                  :::*                    LISTEN      -                   
tcp6       0      0 :::1050                 :::*                    LISTEN      -                   
tcp6       0      0 :::8123                 :::*                    LISTEN      -                   
tcp6       0      0 :::61053                :::*                    LISTEN      -                   
tcp6       0      0 127.0.0.1:7070          :::*                    LISTEN      -                   
tcp6       0      0 :::2181                 :::*                    LISTEN      -                   
tcp6       0      0 127.0.0.1:9990          :::*                    LISTEN      -                   
tcp6       0      0 :::9000                 :::*                    LISTEN      - 
```

DC/OS 의 웹 UI 를 통해 모든 컴포넌트들이 동작하고 있는지 확인합니다.

마스터 서버중 한 곳을 웹 브라우저를 통해 접속해봅니다. [http://52.79.125.242](http://52.79.125.242)

DC/OS 웹 UI 에 로그인하기 위해서는 구글 계정이 필요합니다.

![login](image/install-login.png)

그림과 같이 모든 컴포넌트와 노드들이 health 상태에 있는지 확인합니다.

![health](image/install-health.png)

문제가 있는 컴포넌트가 있다면 원인분석을 위해 [컴포넌트 헬스 체크](trouble-component.md) 를 참조하기 바랍니다.

잘못된 설정값으로 초기 설치를 진행하여, 다시 설치 시도시에 원할히 동작되지 않는 경우 초기 설치요소를 완전히 제거하고 재설치 할 필요가 있습니다. 
[설치 완전 삭제](op-uninstall.md) 를 참조하여 진행하도록 하십시오.



