# 클라우드 패키지 빌드

이제까지 진행해왔던 모든 설치 요소들이 정상적으로 설치되어있는지 확인하길 바랍니다. 

- DC/OS
- Gitlab
- Gitlab CI
- Docker registry

지금부터는 `Devops` 운영 포탈인 `유엔진 클라우드` 를 설치하게 되는데, DC/OS 클러스터와 깃랩 각각의 어드민 토큰이 필요합니다.
  
## Get admin token

DC/OS 클러스터의 어드민 토큰 발급은 앞선 [사용자 생성](infra/install-cluster-user.md) 에서 발급받은 토큰입니다. 
깃랩 어드민 토큰은 깃랩 UI 로 접속하여 받을 수 있습니다. 먼저, 루트계정으로 깃랩으로 접속하도록 합니다. 초기 유저,패스워드는 `root` , `adminadmin` 입니다.

![token](image/package1.png)


## Create Gitlab Projects

클라우드 패키지 설치를 위해서 깃랩에 root 계정으로 다음의 프로젝트를 생성합니다.
 
- cloud-config-repository
- template-iam
- template-springboot
- template-vuejs
- template-zuul

![package2](image/package2.png)

`cloud-config-repository` 프로젝트는 데이터베이스의 역할을 수행하므로, [Fill out Cloud package part](#fill-out-cloud-package-part) 
에서 이 프로젝트의 아이디가 필요합니다. UI 에서 `cloud-config-repository` 프로젝트 화면의 `Settings => General project settings` 
 메뉴로 들어가면 프로젝트 아이디를 볼 수 있습니다.

![package3](image/package3.png)

## Fill out Cloud package part

[클러스터 설치](infra/install-cluster.md) 에서 진행하였던 `uEngine-cloud/uengine-resource/config.yml` 파일의 `Cloud package part`
 설정을 마무리 지어야 합니다.
  
본 파트에서 진행한 `dcos token` , `gitlab token` , `repository id` 를 사용하 다음 항목을들 채우도록 합니다.
 
```
# Cloud package part
# Fill out those properties after install DC/OS cluster && Gitlab
# Then, you should re-command "mvn clean install exec:java package"
dcos: 
  token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiJkYXJrZ29kYXJrZ29AZ21haWwuY29tIiwiZXhwIjozMTI1NTU5MDY0MzkuODk0MX0.aHgH_M-g-n-WlnPg_CorMGYEprULPSeUTIGu3GyZQ-U

gitlab:
  root:
    username: root
    password: adminadmin
    token: -arWnfRY7S4h6oyRthNy
  config-repo:
    projectId: 47
    deployment-path: /deployment
    template-path: /template
```

메이븐 리소스를 통해 설정파일을 재생성합니다.

```
$ cd uEngine-cloud/uengine-resource
$ mvn clean install exec:java package
```

## 데이터 베이스 업로드

설정을 생성하고 나면, 다음과 같은 디렉토리 리스트를 볼수 있습니다.

```
$ cd uEngine-cloud
$ ll

-rw-rw-r--. 1 centos centos  1074  1월 17 16:53 LICENSE
-rw-rw-r--. 1 centos centos 23620  1월 18 06:29 README.md
drwxrwxr-x. 5 centos centos   132  1월 17 17:01 cloud-config-repository
drwxrwxr-x. 2 centos centos   185  1월 17 17:01 deploys
drwxrwxr-x. 3 centos centos  4096  1월 18 06:29 document
drwxrwxr-x. 3 centos centos  4096  1월 17 18:59 install
-rwxrwxr-x. 1 centos centos  2463  1월 17 16:53 pom.xml
drwxrwxr-x. 5 centos centos   140  1월 17 16:53 template-iam
drwxrwxr-x. 4 centos centos   123  1월 17 16:53 template-springboot
drwxrwxr-x. 8 centos centos  4096  1월 17 16:53 template-vuejs
drwxrwxr-x. 4 centos centos   123  1월 17 16:53 template-zuul
drwxrwxr-x. 3 centos centos    50  1월 17 16:53 uengine-cloud-config
drwxrwxr-x. 5 centos centos   118  1월 17 16:53 uengine-cloud-iam
drwxrwxr-x. 3 centos centos    50  1월 17 16:53 uengine-cloud-server
drwxrwxr-x. 7 centos centos   259  1월 17 16:53 uengine-cloud-ui
drwxrwxr-x. 3 centos centos    50  1월 17 16:53 uengine-eureka-server
drwxrwxr-x. 8 centos centos   230  1월 18 06:31 uengine-resource
```

이 중 `cloud-config-repository` 과 `template-*` 로 시작되는 프로젝트들을 [Create Gitlab Projects](#create-gitlab-projects) 
 단계에서 만들어 두었던 프로젝트로 업로드 해야 합니다.
 
업로드 중 유저이름, 패스워드를 묻는 프롬프트가 뜰 경우 깃랩 루트 유저네임과 패스워드를 입력하시면 됩니다.
 
``` 
$ cd cloud-config-repository
$ git init
$ git remote add origin http://gitlab.pas-mini.io/root/cloud-config-repository.git
$ git add .
$ git commit -m "Initial commit"
$ git push -u origin master

$ cd ../template-springboot
$ git init
$ git remote add origin http://gitlab.pas-mini.io/root/template-springboot.git
$ git add .
$ git commit -m "Initial commit"
$ git push -u origin master


$ cd ../template-iam
$ git init
$ git remote add origin http://gitlab.pas-mini.io/root/template-iam.git
$ git add .
$ git commit -m "Initial commit"
$ git push -u origin master

$ cd ../template-zuul
$ git init
$ git remote add origin http://gitlab.pas-mini.io/root/template-zuul.git
$ git add .
$ git commit -m "Initial commit"
$ git push -u origin master

$ cd ../template-vuejs
$ git init
$ git remote add origin http://gitlab.pas-mini.io/root/template-vuejs.git
$ git add .
$ git commit -m "Initial commit"
$ git push -u origin master
```

## 클라우드 패키지 빌드

`uEngine-cloud/install` 폴더에서, `sh docker-build.sh` 를 수행하도록 합니다.

이 스크립트는 `config.yml` 의 환경설정에 따라 커스터마이징 된 소스코드를 빌드하고, 도커 이미지 생성 후, 지정된 도커 레파지토리로 업로드합니다.

```
$ cd uEngine-cloud/install
$ sh docker-build.sh
```

## 클라우드 서버 로컬 테스트

클라우드 서버, 클라우드 UI 코드 로컬 개발 방법입니다. config.yml 의 사전세팅이 필요하며, uengine-cloud-iam 과 config-server 가 
DC/OS 클러스터에 사전 설치되야 합니다. 아래 절차를 진행할 경우 설치된 DC/OS 클러스터 환경을 바라보며 작업이 가능합니다.


먼저 앞선 config.yml 의 설정내용을 빌드합니다.
 
```
$ cd uengine-resource
$ mvn clean install exec:java package
```

유레카 서버를 실행합니다.

```
$ cd uengine-eureka-server
$ mvn install
$ java -jar target/uengine-eureka-server-1.0-SNAPSHOT.jar
```

클라우드 서버를 실행합니다.

```
$ cd uengine-cloud-server
$ mvn install
$ java -jar target/uengine-cloud-server-1.0-SNAPSHOT.jar
```

## 클라우드 UI 로컬 테스트

`uengine-cloud-ui` 의 `index.html` 파일을 클라우드 서버 경로를 로컬 또는 클러스터 를 바라보도록 설정합니다. 

<클러스터>

```
var backendUrl = 'http://cloud-server.pas-mini.io';
//var backendUrl = 'http://localhost:8080';
```

<로컬>

```
//var backendUrl = 'http://cloud-server.pas-mini.io';
var backendUrl = 'http://localhost:8080';
```

인스톨 후 실행합니다.

```
$ cd uengine-cloud-ui
$ npm install
$ npm run dev
```











