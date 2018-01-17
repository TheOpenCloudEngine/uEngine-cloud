## 사용자 생성

DC/OS 엔터프라이즈에서는 많은 시큐리티 설정 옵션이 있지만, 
DC/OS 커뮤니티 버젼은 Oauth0 클라우드 서비스를 통해서만 웹 UI 및 Rest Api 를 접속합니다. (로그아웃 주기도 매우 짧습니다.)

해당 문제를 해결하기 위해, 아래의 로직에 따라 거의 영구적으로 사용할 수 있는 어세스 토큰을 생성하도록 하겠습니다.


1. 마스터 노드 중 한곳으로 이동 후, 영구적인 토큰을 생성합니다. gmail 계정이 하나 필요합니다.

```
$ ssh -i <your-key-file> master1

# 사용자 생성
$ sudo su
# dcos-shell /opt/mesosphere/bin/dcos_add_user.py darkgodarkgo@gmail.com

# 영구적 토큰 생성 워크어라운드
# export DCOS_USER=darkgodarkgo@gmail.com
# DCOS_ACS_TOKEN="$(docker run --rm -v /var/lib/dcos/dcos-oauth/auth-token-secret:/key karlkfi/jwt-encoder ${DCOS_USER} /key --duration=86400000)"
# echo $DCOS_ACS_TOKEN

# 토큰 테스트
# curl --header "Authorization: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiJkYXJrZ29kYXJrZ29AZ21haWwuY29tIiwiZXhwIjozMTI1NTU5MDY0MzkuODk0MX0.aHgH_M-g-n-WlnPg_CorMGYEprULPSeUTIGu3GyZQ-U" http://localhost/service/marathon/v2/apps
```

획득한 토큰은 깃랩 데이터베이스 설치에 필요하니 기억하고 있도록 합니다.


## CLI 설치

부트스트랩 노드로 돌아와서, 다음을 실행합니다.

```
$ cd
$ curl -O https://downloads.dcos.io/binaries/cli/linux/x86-64/dcos-1.10/dcos
$ sudo mv dcos /usr/local/bin
$ chmod +x /usr/local/bin/dcos

# 마스터 노드 중 하나로 cli 를 등록하도록 합니다.
# CLI 클라이언트는 사용자 등록을 위해 웹브라우저 주소를 리다이렉트 합니다. 그러므로, 마스터 노드의 퍼블릭 아이피를 넣어주도록 합니다.
# 네트워크 환경이 프라이빗 아이피로도 마스터 노드로의 브라우저 접속이 가능하다면, 프라이빗 아이피 그대로 사용하셔도 무방합니다.

dcos cluster setup http://52.79.125.242

If your browser didn't open, please go to the following link:

    http://52.79.125.242/login?redirect_uri=urn:ietf:wg:oauth:2.0:oob
Enter OpenID Connect ID Token: 
```

![health](image/install-token.png)

콘솔의 url 로 이동하여, 화면에 나오는 토큰값을 커맨드 프롬프트에 입력하면 CLI 를 사용가능 합니다.




