# 클라우드 패키지 빌드

이제까지 진행해왔던 모든 설치 요소들이 정상적으로 설치되어있는지 확인하길 바랍니다. 

- DC/OS
- Gitlab
- Gitlab CI
- Docker registry

지금부터는 `Devops` 운영 포탈인 `유엔진 클라우드` 를 설치하게 되는데, DC/OS 클러스터와 깃랩 각각의 어드민 토큰이 필요합니다.
  
## Get admin token

DC/OS 클러스터의 어드민 토큰 발급은 앞선 [사용자 생성](install-cluster-user.md) 에서 발급받은 토큰입니다. 
깃랩 어드민 토큰은 깃랩 UI 로 접속하여 받을 수 있습니다. 먼저, 루트계정으로 깃랩으로 접속하도록 합니다. 초기 유저,패스워드는 `root` , `adminadmin` 입니다.

![token](image/package1.png)


## Create Gitlab Projects

클라우드 패키지 설치를 위해서 깃랩에 root 계정으로 다음의 프로젝트를 생성합니다.
 
- cloud-config-repository
- template-iam
- template-springboot
- template-vuejs
- template-zuul

![token](image/package2.png)

`cloud-config-repository` 프로젝트는 데이터베이스의 역할을 수행하므로, [Fill out Cloud package part](#fill-out-cloud-package-part) 
에서 이 프로젝트의 아이디가 필요합니다. UI 에서 `cloud-config-repository` 프로젝트 화면의 `Settings => General project settings` 
 메뉴로 들어가면 프로젝트 아이디를 볼 수 있습니다.

![token](image/package3.png)

## Fill out Cloud package part

[클러스터 설치](install-cluster.md) 에서 진행하였던 `uEngine-cloud/uengine-resource/config.yml` 파일의 `Cloud package part`
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







