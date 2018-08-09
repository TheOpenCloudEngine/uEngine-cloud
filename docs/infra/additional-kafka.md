# Install kafka

Kafka 는 클라우드 플랫폼의 이벤트큐 의 역할을 수행합니다.

아래 스크립트를 통해 카프카 싱글 브로커를 `public` 노드 또는 별도의 kafka 노드를 준비하여 설치하도록 합니다.

**Mode to public**

```
$ ssh -i <key> public
```

**Create docker-compose.yml**

만일 `public` 노드의 아이피가 172.31.5.136 일 경우, 아래와 같이 docker-compose.yml 파일을 작성한 후 실행하도록 합니다.


```
version: '3.3'
services:
  kafka:
    image: wurstmeister/kafka
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      - KAFKA_ADVERTISED_HOST_NAME=172.31.5.136
      - KAFKA_ADVERTISED_PORT=9092
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
    depends_on:
      - zookeeper

  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
    environment:
      - KAFKA_ADVERTISED_HOST_NAME=zookeeper
```

**Install docker-compose** 
```
$ sudo curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
$ sudo chmod +x /usr/local/bin/docker-compose
```

**Run kafka**

```
$ sudo /usr/local/bin/docker-compose up -d
```








