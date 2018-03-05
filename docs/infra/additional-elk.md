# Install ELK

## Overview

**ELK 를 활용한 Docker Log 수집**

![](infra/image/elk-1.png)

유엔진 클라우드에서 지원하는 ELK Stack 의 구성요소는 다음과 같습니다. (Packetbeat 은 추후 예정입니다.)

| 모듈          | 버젼  | 위치       | 역할             |
|---------------|-------|------------|------------------|
| Elasticsearch | 6.2.1 | ELK 서버   | 데이터베이스     |
| Logstash      | 6.2.1 | ELK 서버   | 로그 파서        |
| Kibana        | 6.2.1 | ELK 서버   | 로그 대쉬보드    |
| Filebeat      | 6.2.1 | Agent 노드 | 앱 로그 수집기 |
| Metricbeat    | 6.2.1 | Agent 노드 | 메트릭 데이터 수집기    |
| Packetbeat    | 6.2.1 | Agent 노드 | 앱 트랜잭션 수집기(To-Be)  |

유엔진 클라우드 플랫폼은 ELK 6.1 이상 버젼과 호환됩니다. 별도의 CentOS VM 을 생성하여 설치를 진행하시길 바랍니다. 
이 VM 에는 ELK 구성 요소인 Elasticsearch , Logstash, Kibana 가 설치됩니다. 

## ELK Download

[https://github.com/TheOpenCloudEngine/uEngine-cloud-elk-compose](https://github.com/TheOpenCloudEngine/uEngine-cloud-elk-compose) 
에서 소스코드를 내려받습니다.

```
$ git clone https://github.com/TheOpenCloudEngine/uEngine-cloud-elk-compose 
```

해당 소스코드는 `Docker-compose` 를 통해 elk 스택을 설치하게 됩니다. `Docker-compose` 설치에 관해 [https://docs.docker.com/compose/](https://docs.docker.com/compose/)
 링크를 참조하세요.
 
## ELK Configuration && Run

소스코드의 `docker-compose.yml` 파일을 통해 ELk Docker 의 환경설정을 할 수 있습니다.

**Elasticsearch Volume**

엘라스틱 서치의 데이터 볼륨 설정은 컨테이너의 `/usr/share/elasticsearch/data` 을 통합니다. 아래는 볼륨 마운트 예시입니다.

*데이터 볼륨 생성*

```
$ mkdir /home/centos/data-elk
$ chmod 777 /home/centos/data-elk
```

*docker-compose.yml*

```
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch-oss:6.2.1
    volumes:
      - ./elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml:ro
      - /home/centos/data-elk:/usr/share/elasticsearch/data
```

**Elasticsearch JAVA_OPTS**

엘라스틱 서치의 `JAVA_OPTS` 설정은 운영할 앱의 로그처리량, DC/OS 클러스터의 사이즈 등에 따라 메모리 용량을 늘이는 것이 좋습니다. 초기값은 1G 에 설정되어 있습니다.

*docker-compose.yml*

```
services:
  elasticsearch:
    environment:
      ES_JAVA_OPTS: "-Xmx1024m -Xms1024m"
```

**Run**

다음 명령어로 구동합니다.

```
$ docker-compose up
```

데몬 모드 구동을 위해서는 아래 명령어를 사용합니다.

```
$ docker-compose up -d
```

수분에 걸쳐 Elasticsearch 와 Kibana 가 구동되면, [http://localhost:5601](http://localhost:5601) 을 통해 `Kibana web UI` 로 접근가능합니다.

기본적으로, docker stack 은 다음의 포트 리스트를 노출합니다.:

* 5000: Logstash TCP input.
* 9200: Elasticsearch HTTP
* 9300: Elasticsearch TCP transport
* 5601: Kibana

**WARNING**: 만약 `boot2docker` 를 사용한다면, `localhost` 대신 `boot2docker` IP address 를 사용하세요.

**WARNING**: 만약 `Docker Toolbox` 를 사용한다면, `localhost` 대신 `docker-machine` IP address 를 사용하세요.


## How can I scale out the Elasticsearch cluster?

이 문서의 소스코드의 `Docker-compose` 스택은 테스트를 위한 간이 환경이므로, 프로덕션 환경에서는 Elasticsearch 샤딩을 통한 클러스터 환경을 구축하기 바랍니다: 
[Scaling out Elasticsearch](https://github.com/deviantony/docker-elk/wiki/Elasticsearch-cluster)


## Copy Beats to all nodes

`config.yml` 파일을 다음과 같이 수정합니다. `private` 에 해당 스택의 주소를 넣고, `web` 에는 외부에서 접속 가능한 주소를 기입합니다.

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

**설정 파일 빌드**

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

**publish all nodes**

다음을 실행하면 모든 노드에 `Metricbeat,Filebeat` 에이전트가 설치되고 ELK 로 데이터를 전송하기 시작합니다.

```
$ cd install
$ ansible-playbook ansible-elk.yml
```

## Update Kibana Dashboad

`Metricbeat,Filebeat` 이 Agent 노드에서 실행이 되면, 키바나의 `Management > index Patterns` 메뉴에는 다음과 같은 index 가 자동으로 등록이 됩니다.

![](infra/image/elk-2.png)

만일 자동으로 등록이 되지 않을 경우나, 운용중에 index 를 삭제한 경우, Agent 노드 중 하나에서 다음의 명령어로 `index Patterns` 및 `Saved Objects` 를 
재생성 할 수 있습니다.

**Filebeat setup index and dashboards**

```
$ sudo filebeat setup --dashboards
```

**Metricbeat setup index and dashboards**

```
$ sudo metricbeat setup --dashboards
```

## Update uEngine Dashboard

지금까지 진행한 ELK 스택만으로도 엘라스틱 서치의 Rest api 를 활용한다면 많은 유용한 데이터를 쿼리할 수 있습니다. 유엔진 클라우드에서는 앱별, 스테이지별(dev,stg,prod) 
별 로그와, 이에 해당하는 도커 컨테이너들의 메트릭 합산을 제공하는 대쉬보드를 제공합니다.

다음의 절차를 통해 Kibana 에 대쉬보드를 등록하세요.

**import dashboard**

키바나의 `Management > Saved Objects` 메뉴에 들어가, 우측 Import 버튼을 클릭합니다.

![](infra/image/elk-3.png)

유엔진 클라우드 소스코드의 `install/kibana/objects.json` 파일을 선택하고, `Yes, overwrite all objects` 를 클릭합니다.

Kibana 화면의 검색창에 'uEngine' 으로 검색시 다음과 같은 검색 결과가 나온다면 인스톨이 완료된 것입니다.

![](infra/image/elk-4.png)




 











