# Install own nexus server

Nexus 는 maven 에서 사용할 수 있는 가장 널리 사용되는 무료 repository 중의 하나로, maven,npm,yum 등 다양한 repository 를 사용할 수 있습니다.

Local 에 nexus 를 설치하게 되면, 외부로 부터 dependency 를 끌어 오는 수고를 덜고, local nexus 를 proxy (cache)로 사용함으로써 빠르게 라이브러리들을 끌어 올 수 도 있고, 
반대로 개발팀내에서 사용하는 공통 라이브러리들을 local nexus 에 배포해서 팀간에 공유할 수 도 있습니다.

또한 사용자 계정 지정을 통해서 repository 에 대한 접근 정책을 정의할 수 도 있습니다. 
Nexus 는 repository 의 용도와 목적에 따라서 몇 가지로 나눌 수 있는데, 대표적으로 다음과 같은 종류 들이 있습니다.

---
 1. Snapshots : 빌드등 수시로 릴리즈 되는 바이너리를 배포 하는 장소
 2. Releases : 정식 릴리즈를 통해서 배포되는 바이너리를 저장하는 저장소
 3. 3rd party : 벤더등에서 배포하는 (Oracle,IBM등) 바이너리를 저장해놓는 장소로 특정 솔루션등을 사용할때, 딸려 오는 라이브러리등을 여기에 놓고 사용한다
 4. Proxy Repository : 원격에 원본 repository가 있는 경우, Local에 캐쉬 용도로 사용한다.
 5. Virtual Repository : Repository Group은 몇 개의 repository를 하나의 repository로 묶어서 단일 접근 URL을 제공한다.
---
유엔진 클라우드 플랫폼은 Nexus 2.14 기준으로 작성되었습니다. 신규 버젼은 Nexus 3 이지만, Nexus 3 부터는 Blob 형태의 스토리지를 사용하도록 강제하기 때문에, 
기존 트리 구조 형태의 m2 레파지토리를 그대로 사용할 수 있었던 이전 버젼의 편의성을 위해 2.14 버젼을 사용토록 합니다.

## Docker Install

클라우드 플랫폼 상에 Docker 로 넥서스를 구축하기 위해서, 아래 설치 젍차를 사전에 진행하도록 합니다.

- [유틸리티 설치](infra/install-util.md)
- [클러스터 설치](infra/install-cluster.md)
- [클러스터 계정 및 CLI](infra/install-cluster-user.md)
- [깃랩 && 도커 레지스트리 설치](infra/install-gitlab.md)
- [필수 도커 업로드](infra/additional-docker-upload.md)

### Configuration Docker Nexus

`config.yml` 파일의 `Nexus part` 부분을 설정합니다. 이때, `nexus.ip` 는 `agent` 노드 중 하나를 선택하여 기입하도록 합니다. 
넥서스 도커는 기입한 `agent` 노드에 설치되게 됩니다. `http://nexus.pas-mini.io/~` 는 자신의 도메인 주소로 변경하시기 바랍니다. 
`ex) http://nexus.your-domain.com/~`

```
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
```


### Use uEngine pre-built nexus repository

유엔진 클라우드에서 제공하는 SpringBoot 및 VueJS 앱 제작을 위한 디펜던시를 제공하는 파일이 있습니다. 이 파일을 사용하실 경우 빠른 빌드 속도를 보장하고, 
필수 maven,npm proxy 서버가 사전에 정의되어 있습니다. 

[https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/sonatype-work.zip](https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/sonatype-work.zip) 
주소로 다운로드 받을 수 있습니다.

넥서스를 설치할 `agent` 노드로 이동하신 후, 아래의 스크립트를 실행합니다.

```
$ sudo mkdir /nexus2
$ cd /nexus2
$ sudo wget https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/sonatype-work.zip
$ sudo unzip sonatype-work.zip

$ sudo chown -R 200 /nexus2/sonatype-work
```

설치 후 `/nexus2` 폴더의 디렉토리 구조는 다음과 같습니다.

```
nexus2
└── sonatype-work
    ├── backup
    ├── conf
    ├── db
    ├── felix-cache
    ├── health-check
    ├── indexer
    ├── logs
    ├── nexus.lock
    ├── nuget
    ├── orient
    ├── plugin-repository
    ├── storage
    ├── timeline
    └── trash
```

### Deploy Nexus

다시 bootstrap 노드로 돌아와서 설정 파일 빌드를 합니다.

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

빌드 후에 `deploys/nexus.json` 파일이 생성된 것을 확인합니다.

```
{
  "id": "/uengine-nexus",
  "cmd": null,
  "cpus": 0.5,
  "mem": 650,
  "disk": 0,
  "instances": 1,
  "constraints": [
    [
      "hostname",
      "LIKE",
      "172.31.10.202"
    ]
  ],
  .
  .
}
```

---
**주의** : `DC/OS` 클러스터에 에 nexus 배포 이전에, `marathon lb` 앱이 사전 설치되어야 합니다.

```
$ cd deploys
$ dcos marathon app add marathon-lb.json
$ dcos marathon app add marathon-lb-internal.json
```  
---

`DC/OS` 클러스터에 배포하도록 합니다. 수분 후, `DC/OS` UI 에서 디플로이가 종료되면 `http://nexus.your-domain.com/nexus` 으로 접근하여 확인이 가능합니다.

```
$ dcos marathon app add nexus.json
```

![](image/nexus.png)  


### Use manually setting nexus repository

유엔진에서 제공하는 디펜던시 파일을 사용하지 않을 경우에는 넥서스 설치 후 직접 proxy 레파지토리를 설정해야 합니다.

---
넥서스 `agent` 노드: `/nexus2/sonatype-work` 폴더 생성 및 권한 설정

```
$ sudo mkdir -p /nexus2/sonatype-work
$ sudo chown -R 200 /nexus2/sonatype-work
```
---
| Name            | type   | Format | Repository Policy | Remote Storage Location                         |
|-----------------|--------|--------|-------------------|-------------------------------------------------|
| Releases        | hosted | maven2 | Release           |                                                 |
| Snapshots       | hosted | maven2 | Snapshot          |                                                 |
| Central         | proxy  | maven2 | Release           | https://repo1.maven.org/maven2/                 |
| spring-releases | proxy  | maven2 | Release           | https://repo.spring.io/libs-release/            |
| oss-rh          | proxy  | maven2 | Release           | https://oss.sonatype.org/content/groups/public/ |
| 3rd party       | hosted | maven2 | Release           |                                                 |
| npm-private     | hosted | npm    | Mixed             |                                                 |
| npmjs           | proxy  | npm    | Mixed             | http://registry.npmjs.org/                      |

---
| Name                | type  | Format | Ordered Group Repositories                                       |
|---------------------|-------|--------|------------------------------------------------------------------|
| Public Repositories | group | maven2 | Releases, Snapshots, Central, spring-releases, oss-rh, 3rd party |
| Public Npm          | group | npm    | npm-private, npmjs                                               |


## Typical install

Docker 가 아닌 일반 OS 에 직접 설치를 진행하실 경우 아래 설치파일을 받은 후 압축을 풀도록 합니다. 넥서스는 Java8 이상을 필요로 하니 실행 전 확인하도록 합니다.

- Mac os x / Linux : [다운로드](https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/nexus-2.14.6-02-bundle.tar.gz)
- Window : [다운로드](https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/nexus-2.14.6-02-bundle.zip)

### Window 

관리자 권한으로 cmd 창을 실행시킵니다.

```
./bin/nexus install

./bin/nexus start
```

### Mac / Linux

```
./bin/nexus start
```

















