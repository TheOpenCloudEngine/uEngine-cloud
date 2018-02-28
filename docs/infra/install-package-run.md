# 클라우드 패키지 실행

`uEngine-cloud/deploys` 폴더에는 다음과 같은 파일들이 생성되 있을 것입니다.

```
$ cd uEngine-cloud/deploys
$ ll

-rw-rw-r--. 1 centos centos  1077  1월 18 06:31 cloud-server.json
-rw-rw-r--. 1 centos centos  1367  1월 18 06:31 cloud-ui.json
-rw-rw-r--. 1 centos centos  1656  1월 18 06:31 config.json
-rw-rw-r--. 1 centos centos  1665  1월 18 06:31 db.json
-rw-rw-r--. 1 centos centos  1457  1월 18 06:31 eureka-server.json
-rw-rw-r--. 1 centos centos  1061  1월 18 06:31 iam.json
-rw-rw-r--. 1 centos centos 34932  1월 18 06:31 marathon-lb-internal.json
-rw-rw-r--. 1 centos centos 34885  1월 18 06:31 marathon-lb.json
```

이 파일들은 DC/OS 클러스터에 배포하기 위한 [marathon-parameters](https://docs.mesosphere.com/1.10/deploying-services/marathon-parameters/#example) 파일입니다.

다음 순차로 배포를 시작합니다.

```
# Run these three apps.
dcos marathon app add config.json
dcos marathon app add eureka-server.json
dcos marathon app add db.json
```

먼저, `config.json` , `eureka-server.json` , `db.json` 을 배포하도록 합니다.

- config.json : 클라우드 콘피그 서버
- eureka-server.json : 유레카 서버
- db.json : Mysql 데이터베이스

이후, DC/OS ui 로 접속하여 세가지 서비스가 모두 health 상태가 될때까지 기다리도록 합니다. 이 과정은 수분이 소요될 수 있습니다.
 혹시 Failed 된 Task 가 발생하게 된다면 로그를 확인 후 조치를 취하도록 합니다. 

그 이후, 나머지 서비스들을 배포합니다.

```
# Check pre three apps are all healthy, then run under three apps.
dcos marathon app add iam.json
dcos marathon app add cloud-server.json
dcos marathon app add cloud-ui.json
```

나머지 서비스도 모두 health 상태가 되었다면, [도메인 && 네트워크 준비](pre-domain.md) 에서 설정한 iam 호스트주소로 접속합니다. (ex. **http://iam.pas-mini.io**)

인증 서버의 로그인 창 아이디, 패스워드는 [클러스터 설치 - Security part](install-cluster.md#security-part) 에서 설정한 정보입니다.
 여기서 별다른 변경을 하지 않았다면, `admin` , `admin` 이 아이디, 패스워드 입니다. 로그인을 하시고 난 후 사용자 추가 버튼 클릭 후, 클라우드 플랫폼에 
 접속 가능한 인원을 편집하십시오.
 
![package4](infra/image/package4.png)

여기서, `metadata` 항목의 `acl` 은 `admin` , `user` 가 있습니다. `admin` 은 모든 앱 및 클라우드 패키지, 그리고 도커 서비스를 편집할 수 있는 권한이 있고,
 `user` 는 자신이 생성한 앱, 또는 같은 깃랩 그룹에 소속된 인원의 앱만을 편집할 수 있습니다.
 
