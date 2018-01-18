# 깃랩

다음 절차는 깃랩 서버에서 진행하게 됩니다.

```
$ ssh -i <your-private-key> gitlab
```

깃랩 서버는 클라우드 플랫폼의 앱 정보, 환경설정 정보, 그리고 소스코드 형상관리에 사용되어집니다.

## Omnibus install

깃랩의 설치 방식에는 Omnibus 패키지와 메뉴얼 설치가 있습니다.

Omnibus 패키지 로 설치를 진행하실 경우 깃랩 패키지에 포함된 Chef 프로비져닝 툴에 의해 

- Postgres 데이터베이스 
- 사이드킥 (큐 서버)
- 메일 서버
- 깃
- 루비 레일스
- 캐쉬 서버

들이 함께 설치되게 됩니다.

Omnibus 패키지는 또한 Daily 백업, 리스토어에 있어 편리한 인터페이스를 제공합니다.
 
다음의 명령어로 설치를 진행하도록 합니다.

```
$ sudo yum install -y curl policycoreutils-python openssh-server
$ sudo systemctl enable sshd
$ sudo systemctl start sshd
$ sudo firewall-cmd --permanent --add-service=http
$ sudo systemctl reload firewalld

$ curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.rpm.sh | sudo bash
 
$ sudo yum install -y gitlab-ce-10.2.4-ce.0.el7.x86_64
```

## 기본 환경 설정

깃랩은 IP 로 접속하는 것 보다, FQDN 형식의 external_url 을 설정하여야 프로젝트 주소 및 Push,Commit Remote url 관리시 편리함이 있습니다.

또한 이메일 서버 정보가 있으면, 자신이 수행한 CI 작업 실패시 알림메일을 받을 수 있습니다.

- /etc/gitlab/gitlab.rb 파일을 수정합니다. 처음에는 모두 주석처리되어있으므로, 파일의 가장 상단에 아래의 내용을 설치환경에 맞게 넣어주면 됩니다.

```
$ sudo vi /etc/gitlab/gitlab.rb

.
.
external_url 'http://gitlab.pas-mini.io'

gitlab_rails['smtp_enable'] = true
gitlab_rails['smtp_address'] = "smtp.gmail.com"
gitlab_rails['smtp_port'] = 587
gitlab_rails['smtp_user_name'] = "flamingo.workflow@gmail.com"
gitlab_rails['smtp_password'] = "princoprinco9"
gitlab_rails['smtp_domain'] = "smtp.gmail.com"
gitlab_rails['smtp_authentication'] = "login"
gitlab_rails['smtp_enable_starttls_auto'] = true
gitlab_rails['smtp_tls'] = false
.
.
```

설정이 끝난 후에는 reconfigure 를 수행하도록 합니다. 이는 Chef 클라이언트가 설정대로 컴포넌트를 재배포하여, 깃랩 Rails 서버를 재구동시킵니다.

```
$ sudo gitlab-ctl reconfigure
```


# 도커 레지스트리 설치

다음은 도커 레지스트리 (도커 이미지 저장소) 설치과정입니다. 깃랩과 동일한 서버에서 진행하도록 합니다.

도커 레지스트리는 깃랩 CI 에서 생성된 도커이미지를 저장하였다가, 클라우드 플랫폼에서 배포 요청시 이미지를 보내주는 역할을 합니다. 
아래 명령어는 항시적으로 registry 서버가 떠있도록 해줍니다.

```
sudo docker run -d -p 5000:5000 --restart=always --name registry registry:2
```



