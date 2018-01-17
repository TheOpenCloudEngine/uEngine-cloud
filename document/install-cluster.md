# 클러스터 설치

## 설치파일 다운로드

하기 진행순서부터는 root 계정이 아닌, 리눅스 사용자 계정으로 진행하시길 바랍니다 (ex. centos)

설치에 필요한 파일을 다운로드 받습니다.

```
$ cd

# 유엔진 클라우드 다운로드
$ git clone https://github.com/TheOpenCloudEngine/uEngine-cloud

# DCOS 인스톨 파일 다운로드
$ wget https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/dcos_generate_config.sh
```

## 설정 파일 편집

유엔진 클라우드는 앤서블 유틸리티를 사용해 설치자동화를 지원하고, 클라우드 패키지 플랫폼 구성요소들을 빌드합니다.

모든 설정이 하나의 파일에서 이루어지는데, 다음의 경로에 위치해 있습니다.

- uEngine-cloud/uengine-resource/config.yml

아래는, 설정 파일의 각 파트별 설명입니다.
 
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

여기서 resolvers 와 dns_search 는 다소 생소할 수 있지만, /etc/resolv.conf 조회를 통해 간단히 알아낼 수 있습니다.

```
# cat /etc/resolv.conf

search ap-northeast-2.compute.internal
nameserver 172.31.0.2
```

- nameserver 개수만큼 resolvers 에 넣습니다.
- search 가 있다면 dns_search 에 넣고, 없다면 공란으로 비워둡니다.


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

### Security(IAM) part

사용자 인증정보에 관한 설정입니다.

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
    password: !gosu23546
    port: 587
    smtp-auth: true
    smtp-starttls-enable: true
    from-address: sppark@uengine.org
    from-name: uengine
```
 

## 설정 파일 빌드

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

