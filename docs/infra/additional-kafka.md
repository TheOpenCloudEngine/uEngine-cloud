# Install kafka

Kafka 는 클라우드 플랫폼의 이벤트큐 의 역할을 수행합니다.

아래 스크립트를 통해 카프카 싱글 브로커를 `public` 노드에 설치하도록 합니다.

**Mode to public**

```
$ ssh -i <key> public
```

**Run kafka**

만일 `public` 노드의 아이피가 172.31.5.136 일 경우, 아래와 같이 도커를 실행하도록 합니다.

```
sudo docker run -d --name kafka -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=172.31.5.136 --env ADVERTISED_PORT=9092 --env NUM_PARTITIONS=3 sppark/kafka:v1
```








