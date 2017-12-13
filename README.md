# 설치과정

## 사전 준비 사항

 — 모든 노드는 CentOs/Rhel 7.2 ~ 7.3 OS 에서 구동됩니다.  
 - 내부망 UDP 뚤려있을것: UDP 가 뚤려있지 않으면 53 포트 domain resolve 가 작동하지 않는다. (운영시)
 - 내부망 TCP 포트는 모두 뚤려있을것 (운영시)
 - 외부망 인바운드 :  퍼블릭 에이전트 머신의 80 포트와 443 포트 (운영시)
 - 외부망 아웃바운드 : 모두 뚤려있을것 (설치시)
 - 네임서버 :  *.<server>.<domain>  형식으로, A MASK 가  *  로 설정된 도메인을 보유하고있을것. (운영시)
 - 모든 서버는 동일한 pem 파일로 ssh 가 가능할 것. (운영시)
 - 모든 서버의 /etc/ssh/ssh_config 는 PermitRootLogin (설치시) 을 허용할 것.


## DCOS

### 서버 준비

- 다음의 서버들을 준비해야 합니다. (사양은 추천사항입니다.)
- 마스터 노드는 홀수개의 서버로 준비하도록 합니다. (1,3,5)
- 퍼블릭 에이전트 노드는 최소 한개를 구성해야 합니다.
- 퍼블릭 에이전트 노드의 80 포트와 443 포트는 외부에서 접속이 가능해야 합니다.


| 역할 / 호스트네임 | 사양                     | IP 주소      |
|-------------------|--------------------------|--------------|
| bootstrap         | 2 CPU /2 GB/10 GB Disk   | 192.168.0.25 |
| master1           | 2 CPU /4 GB/20 GB Disk   | 192.168.0.39 |
| master2           | 2 CPU /4 GB/20 GB Disk   | 192.168.0.23 |
| master3           | 2 CPU /4 GB/20 GB Disk   | 192.168.0.8  |
| public-agent      | 4 CPU /8 GB/20 GB Disk   | 192.168.0.37 |
| agent1            | 4 CPU /8 GB/20 GB Disk   | 192.168.0.27 |
| agent2            | 4 CPU /8 GB/20 GB Disk   | 192.168.0.28 |
| agent3            | 4 CPU /8 GB/20 GB Disk   | 192.168.0.24 |
| agent4            | 4 CPU /8 GB/20 GB Disk   | 192.168.0.25 |
| agent5            | 4 CPU /8 GB/20 GB Disk   | 192.168.0.31 |
| agent6            | 4 CPU /8 GB/20 GB Disk   | 192.168.0.26 |
| agent7            | 4 CPU /8 GB/20 GB Disk   | 192.168.0.33 |
| gitlab            | 4 CPU /32 GB/200 GB Disk | 192.168.0.35 |

### 호스트 준비

#### 도메인 준비

다음의 도메인들을 준비하도록 합니다. 다음의 보기에서는 pas-mini.io 도메인을 소유했다고 가정하고, 다음의 A_Mask 들을 준비하도록 합니다.

| A_MASK        | 도메인      | 역할                   | 아이피/호스트              |
|---------------|-------------|------------------------|----------------------------|
| gitlab        | pas-mini.io | 깃랩 / 도커 레지스트리 | 192.168.0.35(gitlab)       |
| config        | pas-mini.io | 클라우드 콘피그 서버   | 192.168.0.37(public-agent) |
| eureka-server | pas-mini.io | 유레카 서버            | 192.168.0.37(public-agent) |
| api-dev       | pas-mini.io | 개발 Api-gateway       | 192.168.0.37(public-agent) |
| api-stg       | pas-mini.io | 스테이지 Api-gateway   | 192.168.0.37(public-agent) |
| api           | pas-mini.io | 프로덕션 Api-gateway   | 192.168.0.37(public-agent) |
| cloud-server  | pas-mini.io | 클라우드 플랫폼 서버   | 192.168.0.37(public-agent) |
| cloud         | pas-mini.io | 클라우드 플랫폼 UI     | 192.168.0.37(public-agent) |

#### 호스트네임 변경

모든 서버에 각 역할에 맞는 호스트네임으로 변경합니다. 

예) 192.168.0.25 bootstrap 서버

— RHEL

```
sudo vi /etc/sysconfig/network
HOSTNAME=bootstrap
sudo reboot
```

— CENTOS

```
sudo vi /etc/hostname
bootstrap
sudo hostname bootstrap
```

#### ssh root 접속 허용

모든 서버의 /etc/ssh/ssh_config 의 PermitRootLogin 를 주석처리합니다.

— ssh config

```
sudo vi /etc/ssh/ssh_config
# PermitRootLogin no
sudo service sshd restart
```

### 클러스터 준비

하기의 모든 사항은, bootstrap 노드에서 진행하도록 합니다.

#### 설치파일 다운로드

설치에 필요한 파일을 다운로드 받습니다.

```
git clone https://github.com/TheOpenCloudEngine/uEngine-cloud
wget https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/dcos_generate_config.sh
```

#### 설치 파일 복사

```
cd uEngine-cloud
cp -R ./script ./install 
```

#### 호스트 파일 수정

```
vi ./install/etc-hosts

192.168.0.25  bootstrap
192.168.0.39 master1
192.168.0.23 master2
192.168.0.8 master3
192.168.0.37 public
192.168.0.27 agent1
192.168.0.28 agent2
192.168.0.24 agent3
192.168.0.25 agent4
192.168.0.31 agent5
192.168.0.26 agent6
192.168.0.33 agent7
```

#### ansible 설치

```
sudo yum install epel-release
sudo yum install ansible
```

#### /etc/ansible/hosts 생성

- /etc/ansible/hosts 파일을 생성합니다. (./install/ansible-hosts.yml 파일을 참조)


```
sudo vi /etc/ansible/hosts

[all:vars]
ansible_user=centos
ansible_ssh_private_key_file=/home/centos/dcos-key.pem
registry_host=gitlab.pas-mini.io:5000  # 깃랩 도메인:5000 

[bootstrap]
192.168.0.25

[master]
192.168.0.39
192.168.0.23
192.168.0.8

[public]
192.168.0.37

[agent]
192.168.0.27
192.168.0.28
192.168.0.24
192.168.0.25
192.168.0.31
192.168.0.26
192.168.0.33
```

#### playbook.yml 리뷰

ansible-playbook.yml 을 살펴보면, 다음의 내용으로 이루어져있습니다.

이 플레이북을 실행시키면, DCOS 클러스터를 설치하기 위한 기반 유틸리티 및 시스템 구성이 완료될 것입니다.


```
# vi playbook.yml

---
- hosts: all
  remote_user: "{{ansible_user}}"
  tasks:
    - name: Host file copy
      become: true
      become_method: sudo
      copy:
        src: ./etc-hosts
        dest: /etc/hosts
        owner: root
        group: root
        mode: 0644
    - name: Docker install
      command: "{{ item }}"
      with_items:
        - sudo yum remove docker \
                  docker-common \
                  docker-selinux \
                  docker-engine
        - sudo yum install -y yum-utils \
          device-mapper-persistent-data \
          lvm2
        - sudo yum-config-manager \
          --add-repo \
          https://download.docker.com/linux/centos/docker-ce.repo
        - sudo yum install docker-ce -y

    - name: Utils Install
      command: "{{ item }}"
      with_items:
        - sudo yum install -y ntp tar xz unzip curl ipset bind-utils
        - sudo sed -i s/SELINUX=enforcing/SELINUX=permissive/g /etc/selinux/config
        - sudo yum install haveged -y
        - sudo chkconfig haveged on

    - name: Ntp
      command: "{{ item }}"
      with_items:
        - sudo systemctl enable ntpd
        - sudo service ntpd start

    - name: Group add
      command: "{{ item }}"
      with_items:
        - sudo groupadd nogroup
        - sudo groupadd docker
      ignore_errors: True

- hosts: master:public:agent
  remote_user: "{{ansible_user}}"
  tasks:
    - name: Restart machine
      shell: sleep 2 && reboot
      async: 1
      poll: 0
      ignore_errors: true
      become: true
      become_method: sudo

    - name: Wait 600 seconds, but only start checking after 10 seconds
      wait_for_connection:
        delay: 10
        timeout: 600

- hosts: all
  remote_user: "{{ansible_user}}"
  tasks:
    - name: Docker service file
      become: true
      become_method: sudo
      copy:
        src: ./docker.service
        dest: /usr/lib/systemd/system/docker.service
        owner: root
        group: root
        mode: 0644
    - name: Docker conf file
      become: true
      become_method: sudo
      copy:
        content: |
          ARG1=-H unix:///var/run/docker.sock --insecure-registry {{registry_host}}
        dest: /etc/docker.conf
        owner: root
        group: root
        mode: 0644
    - name: Docker daemon reload
      command: "{{ item }}"
      with_items:
        - sudo systemctl daemon-reload
        - sudo systemctl enable docker
        - sudo systemctl restart docker
      ignore_errors: True

# play book
# ansible-playbook ansible-playbook.yml

# play at task
# ansible-playbook ansible-playbook.yml --start-at-task="Docker service file"
```

#### playbook.yml 실행

다음의 명령어로 앤시블 프로비져닝을 실행합니다.

```
cd install
ansible-playbook ansible-playbook.yml
```

플레이북은 타스크들로 구성되어있으며, 만약 특정 타스크부터 수행하고 싶을 경우 다음의 명령어를 사용합니다.

```
cd install
ansible-playbook ansible-playbook.yml --start-at-task="Docker service file"
```

### 클러스터 설치

#### genconf

```
cd install
mkdir -p genconf
```

#### ip-detect

ip-detect 는, DC/OS 클러스터 실행시 각 노드들이 자신의 아이피를 찾을 때 사용되는 스크립트입니다.

```
vi genconf/ip-detect

#!/usr/bin/env bash
set -o nounset -o errexit
export PATH=/usr/sbin:/usr/bin:$PATH
echo $(ip addr show eth0 | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' | head -1)
```

#### ssh_key

genconf/ssh_key 에 접속가능한 private key pem 파일의 내용을 넣습니다. 

```
vi genconf/ssh_key

<PRIVATE KEY>

chmod 400 genconf/ssh_key
```

#### config.yaml

config.yaml 은 DC/OS 프로비져닝에 필요한 스택구성 파일입니다.

아래의 예제에서 각 아이피 리스트를 역할에 맞게 수정하여 줍니다.

```
vi genconf/config.yaml
---
agent_list:
- 192.168.0.27
- 192.168.0.28
- 192.168.0.24
- 192.168.0.25
- 192.168.0.31
- 192.168.0.26
- 192.168.0.33
bootstrap_url: file:///opt/dcos_install_tmp
cluster_name: uEngine
exhibitor_storage_backend: static
master_discovery: static
ip_detect_public_filename: genconf/ip-detect
ip_detect_path: genconf/ip-detect
master_list:
- 192.168.0.39
- 192.168.0.23
- 192.168.0.8
process_timeout: 10000
public_agent_list:
- 192.168.0.37
resolvers:
- 121.88.255.50
- 121.88.255.49
ssh_key_path: genconf/ssh_key
ssh_port: 22
ssh_user: centos
telemetry_enabled: true
oauth_enabled: true
```

#### resolvers

위의 구성중 resolvers 과 dns_search 항목은, mesos-dns 가 외부 네임스페이스 서버를 Lookup 할 때의 resolver 주소입니다.

모든 서버의 resolvers 가 동일하다고 가정해야 하며, 보통은 OS 의 /etc/resolve.conf 에 정의되어있습니다.
 
/etc/resolve.conf 참조하여 만약 다음의 값이라면

```
search ap-northeast-2.compute.internal
nameserver 172.31.0.2
```

genconf/config.yaml 의 resolvers 는 nameserver 리스트로, dns_search 값은 search 값으로 설정하도록 합니다.

```
resolvers:
- 121.88.255.50
- 121.88.255.49
dns_search: ap-northeast-2.compute.internal
```

/etc/resolve.conf 에 search 가 없다면, genconf/config.yaml 파일에 dns_search 값은 없어도 됩니다. 


#### 프로비져닝

모든 사항이 준비되었다면, 아래 명령어를 차례대로 수행합니다.

- genconf 생성

```
cd install
sudo bash dcos_generate_config.sh --genconf
```

- preflight 는 사전 체크 단계입니다. 이 단계에서 모든 사항에 대해 pass 가 나오지 않는다면, 트러블 슈팅을 통해 해결하세요.

```
cd install
sudo bash dcos_generate_config.sh --preflight
```

- deploy 는 설치 단계입니다. 수분 이상 소요될 수 있습니다.

```
cd install
sudo bash dcos_generate_config.sh --deploy
```

- postflight 는 설치 후 확인 단계입니다. 이 단계에서 모든 DC/OS 컴포넌트의 헬스체크를 하게 되며, 만약 이 단계에서 수분간 동작을 멈추거나 Fail 이 떨어지게 된다면, 트러블 슈팅을 통해 이슈를 해결하셔야 합니다.

```
cd install
sudo bash dcos_generate_config.sh --postflight
```

#### 트러블 슈팅: 프로비져닝 후 동작 로그 확인

 - DC/OS [트러블 슈팅](https://dcos.io/docs/1.10/installing/troubleshooting/) 문서


```
journalctl -flu dcos-exhibitor
journalctl -flu dcos-spartan
journalctl -flu dcos-mesos-dns
journalctl -flu dcos-mesos-slave
journalctl -flu dcos-mesos-slave-public
journalctl -flu dcos-mesos-master
journalctl -flu dcos-gen-resolvconf
```

 - 기존 시스템 구성을 모두 삭제하고 새로 인스톨 할 경우, 다음의 playbook 을 실행하도록 합니다.
 
```
cd install
ansible-playbook ansible-playbook-remove.yml
``` 

#### 사용자 생성

마스터 노드 중 한곳에서 실행하도록 합니다.

```
sudo -i dcos-shell /opt/mesosphere/bin/dcos_add_user.py darkgodarkgo@gmail.com


export DCOS_USER=darkgodarkgo@gmail.com
DCOS_ACS_TOKEN="$(docker run --rm -v /var/lib/dcos/dcos-oauth/auth-token-secret:/key karlkfi/jwt-encoder ${DCOS_USER} /key --duration=86400000)"
echo $DCOS_ACS_TOKEN

# 토큰 테스트
curl --header "Authorization: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiJkYXJrZ29kYXJrZ29AZ21haWwuY29tIiwiZXhwIjozMTI1NTI2NDE5MzYuMjc2M30.IoIANXtSpc000tNFdpDGIC5MlbezSD7ovnellaJMOOs" http://localhost/service/marathon/v2/apps
```

획득한 토큰은 깃랩 데이터베이스 설치에 필요하니 기억하고 있도록 합니다.


#### CLI 설치

```
curl -O https://downloads.dcos.io/binaries/cli/linux/x86-64/dcos-1.10/dcos
sudo mv dcos /usr/local/bin
chmod +x /usr/local/bin/dcos

dcos cluster setup http://192.168.0.14

If your browser didn't open, please go to the following link:

    http://192.168.0.14/login?redirect_uri=urn:ietf:wg:oauth:2.0:oob
Enter OpenID Connect ID Token: 
```

콘솔의 url 로 이동하여, 화면에 나오는 토큰값을 대화창에 입력하면 CLI 를 사용가능 합니다.


### Gitlab

<Gitlab> <Ubuntu>

```
curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.deb.sh | sudo bash


sudo EXTERNAL_URL="http://gitlab.pas-mini.io" apt-get install gitlab-ce


sudo dockerd -H unix:///var/run/docker.sock --insecure-registry gitlab.pas-mini.io:5000 &
```

<Gitlab CI> <Ubuntu>

```
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh | sudo bash

sudo su

cat > /etc/apt/preferences.d/pin-gitlab-runner.pref <<EOF
Explanation: Prefer GitLab provided packages over the Debian native ones
Package: gitlab-runner
Pin: origin packages.gitlab.com
Pin-Priority: 1001
EOF

exit

sudo apt-get install gitlab-runner


sudo gitlab-ci-multi-runner register -n \
  --url http://gitlab.pas-mini.io\
  --registration-token Kndb7ac3-4__EfEwxB_o \
  --executor docker \
  --description "uengine docker runner:shared-socket" \
  --docker-image "docker:latest" \
  --docker-volumes /var/run/docker.sock:/var/run/docker.sock


sudo vi /etc/gitlab-runner/config.toml

concurrent = 5
check_interval = 0

[[runners]]
  name = "uengine docker runner:shared-socket"
  url = "http://gitlab.uengine.io"
  token = "306c411ef28850993d24faa3a05204"
  executor = "docker"
  [runners.docker]
    tls_verify = false
    image = "docker:latest"
    privileged = false
    disable_cache = false
    volumes = ["/root/m2:/root/.m2","/var/run/docker.sock:/var/run/docker.sock", "/cache"]
    shm_size = 0
  [runners.cache]
  
sudo gitlab-ci-multi-runner restart
```

## 데이터베이스 설치

### Node 설치

```
sudo su
curl --silent --location https://rpm.nodesource.com/setup_7.x | bash -
yum install nodejs -y
node -v
npm -v
```

### Java && Maven 설치

#### Java

```
sudo su
cd /opt/
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u151-b12/e758a0de34e24606bca991d704f6dcbf/jdk-8u151-linux-x64.tar.gz"
tar xzf jdk-8u151-linux-x64.tar.gz

cd /opt/jdk1.8.0_151/
alternatives --install /usr/bin/java java /opt/jdk1.8.0_151/bin/java 2
alternatives --config java

alternatives --install /usr/bin/jar jar /opt/jdk1.8.0_151/bin/jar 2
alternatives --install /usr/bin/javac javac /opt/jdk1.8.0_151/bin/javac 2
alternatives --set jar /opt/jdk1.8.0_151/bin/jar
alternatives --set javac /opt/jdk1.8.0_151/bin/javac

vi /etc/profile

export JAVA_HOME=/opt/jdk1.8.0_151
export JRE_HOME=/opt/jdk1.8.0_151/jre
export PATH=$PATH:/opt/jdk1.8.0_151/bin:/opt/jdk1.8.0_151/jre/bin

source /etc/profile
```

#### Maven

```
wget http://apache.mirror.cdnetworks.com/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
tar xvf apache-maven-3.3.9-bin.tar.gz
mv apache-maven-3.3.9  /usr/local/apache-maven

vi /etc/profile

export M2_HOME=/usr/local/apache-maven
export M2=$M2_HOME/bin
export PATH=$M2:$PATH

source /etc/profile
mvn -version
```

### 깃랩 데이터베이스 생성 - 추가 자동화 대상 TODO

### 데이터베이스 설정값 변경 - 추가 자동화 대상 TODO

### 데이터베이스 업로드

```
git config --global user.name "Administrator"
git config --global user.email "sppark@uengine.org"

cd cloud-config-repository
git init
git remote add origin http://gitlab.pas-mini.io/root/cloud-config-repository.git

git add .
git commit -m "Initial commit"
git push -u origin master


cd template-springboot
git init
git remote add origin http://gitlab.pas-mini.io/root/template-springboot.git
git add .
git commit -m "Initial commit"
git push -u origin master


cd template-vuejs
git init
git remote add origin http://gitlab.pas-mini.io/root/template-vuejs.git
git add .
git commit -m "Initial commit"
git push -u origin master
```

### 어플리케이션 실행

#### 마라톤 LB(Haproxy) 실행

```
—— 마라톤 lb external

dcos package describe --config marathon-lb

dcos package install marathon-lb

vi marathon-lb-internal.json

{ "marathon-lb":
   {"name":"marathon-lb-internal","haproxy-group":"internal","bind-http-https":false,"role":"",  "cpus": 1.0, "mem": 1024.0} 
}


-- 마라톤 lb internal

dcos package install --options=marathon-lb-internal.json marathon-lb
```

이후 각각 0.5 , 512 MB 로 줄일것.

marathon-lb 는 특히 반드시 서스펜드 후 리소스 조정을 할것(host 서비스 특성)

#### 클라우드 패키지 실행 json 생성 - 추가 자동화 대상 TODO 


#### 클라우드 패키지 실행

```
-- 클라우드 콘피스 서버와 유레카 서버를 구동시킨다.

dcos marathon app add uengine-cloud-config/deploy.json
dcos marathon app add uengine-eureka-server/deploy.json



-- 위의 두가지가 모두 구동완료되었을 때 나머지를 실행시킨다.

dcos marathon app add uengine-eureka-zuul/deploy-dev-role.json
dcos marathon app add uengine-eureka-zuul/deploy-stg-role.json
dcos marathon app add uengine-eureka-zuul/deploy-prod-role.json
dcos marathon app add uengine-cloud-server/deploy.json
dcos marathon app add uengine-cloud-ui/deploy.json
```

# 운영

## 에이전트 추가

## 에이전트 삭제

## 마스터 추가

## 마스터 삭제

## 트러블 슈팅

## CLI 제작 - TODO CLI 클라이언트 언어 선택(Node JS process 유력)

 - ** 메인 용어 정하기 / 프로덕션 이름 정하기
 - CLI LIST
 
 | snc-cloud                              |
 |-----------------------------------|
 | snc-cloud auth                         |
 | snc-cloud auth login                   |
 | snc-cloud auth logout                  |
 | snc-cloud cluster                      |
 | snc-cloud cluster attach               |
 | snc-cloud cluster list                 |
 | snc-cloud cluster remove               |
 | snc-cloud cluster rename               |
 | snc-cloud cluster setup                |
 | snc-cloud config                       |
 | snc-cloud config set                   |
 | snc-cloud config show                  |
 | snc-cloud config unset                 |
 | snc-cloud config validate              |
 | snc-cloud experimental                 |
 | snc-cloud experimental add             |
 | snc-cloud experimental package build   |
 | snc-cloud experimental service start   |
 | snc-cloud help                         |
 | snc-cloud job                          |
 | snc-cloud job add                      |
 | snc-cloud job history                  |
 | snc-cloud job kill                     |
 | snc-cloud job list                     |
 | snc-cloud job remove                   |
 | snc-cloud job run                      |
 | snc-cloud job schedule add             |
 | snc-cloud job schedule remove          |
 | snc-cloud job schedule show            |
 | snc-cloud job schedule update          |
 | snc-cloud job show                     |
 | snc-cloud job show runs                |
 | snc-cloud job update                   |
 | snc-cloud marathon                     |
 | snc-cloud marathon about               |
 | snc-cloud marathon app add             |
 | snc-cloud marathon app kill            |
 | snc-cloud marathon app list            |
 | snc-cloud marathon app remove          |
 | snc-cloud marathon app restart         |
 | snc-cloud marathon app show            |
 | snc-cloud marathon app start           |
 | snc-cloud marathon app stop            |
 | snc-cloud marathon app update          |
 | snc-cloud marathon app version list    |
 | snc-cloud marathon debug details       |
 | snc-cloud marathon debug list          |
 | snc-cloud marathon debug summary       |
 | snc-cloud marathon deployment list     |
 | snc-cloud marathon deployment rollback |
 | snc-cloud marathon deployment stop     |
 | snc-cloud marathon deployment watch    |
 | snc-cloud marathon group add           |
 | snc-cloud marathon group list          |
 | snc-cloud marathon group remove        |
 | snc-cloud marathon group scale         |
 | snc-cloud marathon group show          |
 | snc-cloud marathon group update        |
 | snc-cloud marathon pod add             |
 | snc-cloud marathon pod kill            |
 | snc-cloud marathon pod list            |
 | snc-cloud marathon pod remove          |
 | snc-cloud marathon pod show            |
 | snc-cloud marathon pod update          |
 | snc-cloud marathon task list           |
 | snc-cloud marathon task show           |
 | snc-cloud marathon task stop           |
 | snc-cloud node                         |
 | snc-cloud node diagnostics             |
 | snc-cloud node diagnostics create      |
 | snc-cloud node diagnostics delete      |
 | snc-cloud node diagnostics download    |
 | snc-cloud node list-components         |
 | snc-cloud node log                     |
 | snc-cloud node ssh                     |
 | snc-cloud package                      |
 | snc-cloud package describe             |
 | snc-cloud package install              |
 | snc-cloud package list                 |
 | snc-cloud package repo add             |
 | snc-cloud package repo list            |
 | snc-cloud package repo remove          |
 | snc-cloud package search               |
 | snc-cloud package uninstall            |
 | snc-cloud package update               |
 | snc-cloud service                      |
 | snc-cloud service log                  |
 | snc-cloud service shutdown             |
 | snc-cloud task                         |
 | snc-cloud task exec                    |
 | snc-cloud task log                     |
 | snc-cloud task ls                      |




