# Install ELK

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




ELK 사진
ELK 노드 생성
ELK 설치 (docker-elk 포크)
config.yml
filebeat,metricbeat 배포
uengine dashboard 업로드










