# 유엔진 클라우드 운영 가이드

- 설치
  - [사전 준비](document/pre-install.md)
  - [네트워크 고정 아이피(옵셔널)](document/static-ips.md)


#### 마스터 로그 디렉토리 마운트

- 마스터 서버는 많은 양의 로그가 발생하게 되므로, /var/log 에 충분한 디스크 공간을 할당하길 바랍니다.
- 슬레이브 노드는 도커가 실행되게 되므로, /var/lib/docker 에 충분한 디스크 공간을 할당하길 바랍니다.

1. 파티션 확인

```
lsblk

sda               8:0    0   20G  0 disk 
├─sda1            8:1    0  500M  0 part /boot
├─sda2            8:2    0  9.5G  0 part 
│ ├─centos-root 253:0    0  8.5G  0 lvm  /
│ └─centos-swap 253:1    0    1G  0 lvm  [SWAP]
sr0              11:0    1  366K  0 rom  
```


2. sda 하위에 파티션을 추가할 경우

```
sudo su
fdisk /dev/sda

Welcome to fdisk (util-linux 2.23.2).

Changes will remain in memory only, until you decide to write them.
Be careful before using the write command.


Command (m for help): n
Partition type:
   p   primary (2 primary, 0 extended, 1 free)
   e   extended
Select (default p): p

Select (default 3,4): 3
Selected partition 3

First sector (39845888-41943039, default 39845888): 
Using default value 39845888
Last sector, +sectors or +size{K,M,G} (39845888-41943039, default 41943039): +30G
Partition 3 of type Linux and of size 30G is set

Command (m for help): w

# reboot

# lsblk
```

3. 디스크 마운트


OS 가 이미 충분한 양의 디스크에서 운용이 되고있다면 이 단계는 넘어가도 됩니다. 하지만 100GB 이하의 디스크 볼륨에서 OS 가 운용되고있거나, 장기적인 운용을 생각한다면 각 서버의 데이터 디렉토리에 대해 별도의 볼륨 디스크를 활용하시길 권장합니다.

- 모든 서버에 100GB 이상의 볼륨을 생성했다고 가정합니다. 생성한 볼륨을 서버별로 다음의 디렉토리에 마운트를 수행하여야 합니다.

- Master : /var/log
- Slave : /var/lib/docker

- 다음 명령어로 마운트를 수행할 수 있습니다.

```
$ sudo lsblk
.
.
NAME    MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
xvda    202:0    0    8G  0 disk
└─xvda1 202:1    0    8G  0 part /
xvdf    202:80  0  100G  0 disk
.
.
마지막의 xvdf 볼륨이 100G 인 것을 확인할 수 있습니다.
해당 볼륨을 ext4 로 포맷합니다.

$ sudo file -s /dev/xvdf
$ sudo mkfs -t ext4 /dev/xvdf
.
.
Done
.
.

1) 에서 정의한 마운트포인트로 마운트합니다.

$ sudo mount /dev/xvdf <mount point>

예시:
$ sudo mount /dev/xvdf /var/lib/docker

df 명령어를 통해 마운트 된 사항을 볼 수 있습니다.
$ sudo df
.
.
Filesystem    1K-blocks    Used Available Use% Mounted on
/dev/xvda1      8115168 2189708  5490184  29% /
none                  4      0        4  0% /sys/fs/cgroup
udev            2009928      12  2009916  1% /dev
tmpfs            404688    404    404284  1% /run
none                5120      0      5120  0% /run/lock
none            2023436    316  2023120  1% /run/shm
none              102400      0    102400  0% /run/user
/dev/xvdf      103081248  448452  97373532  1% /var/lib/docker
.
.

```


- 해당 마운트를 시스템 부팅때 자동으로 수행하도록 설정합니다.

```
/etc/fstab 파일을 백업합니다.
$ sudo cp /etc/fstab /etc/fstab.orig

$ sudo vi /etc/fstab
.
.
/dev/xvdf <mount point> ext4 defaults,nofail 0 2
⇒ 해당 라인을 추가합니다.
.
.
$ sudo mount -a
```


### 클러스터 준비

하기의 모든 사항은, bootstrap 노드에서 진행하도록 합니다.

#### 설치파일 다운로드

설치에 필요한 파일을 다운로드 받습니다.

```
git clone https://github.com/TheOpenCloudEngine/uEngine-cloud
```

#### 설치 파일 복사 및 추가 다운로드

```
sudo yum install wget -y

cd uEngine-cloud
cp -R ./script ./install
cd ./install
wget https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/dcos_generate_config.sh
```

#### 호스트 파일 수정

```
cd ./install
vi ./etc-hosts

127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
::1         localhost localhost.localdomain localhost6 localhost6.localdomain6

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
sudo yum install epel-release -y
sudo yum install ansible -y
```

#### /etc/ansible/hosts 생성

- /etc/ansible/hosts 파일을 생성합니다. (./install/ansible-hosts.yml 파일을 참조)

- 하기 내용중 ansible_ssh_private_key_file 은 ssh 키 파일 위치이며, 퍼미션이 400 이여야 합니다.

```
sudo vi /etc/ansible/hosts

[all:vars]
ansible_user=centos
ansible_ssh_private_key_file=/home/centos/dcos-key.pem
registry_host=gitlab.pas-mini.io:5000

[bootstrap]
192.168.0.7

[gitlab]
192.168.0.35

[master]
192.168.0.14
192.168.0.23
192.168.0.8

[public]
192.168.0.30

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
# vi ansible-playbook.yml

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
    - name: Docker util install
      command: "{{ item }}"
      with_items:
        - sudo yum remove docker \
                  docker-common \
                  docker-selinux \
                  docker-engine
        - sudo yum install -y yum-utils \
          device-mapper-persistent-data \
          lvm2

    - name: Docker repository ready
      become: true
      become_method: sudo
      command: yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
      async: 60
      poll: 5

    - name: Yum update
      command: "{{ item }}"
      with_items:
        - sudo yum update -y

    - name: Docker install
      command: "{{ item }}"
      with_items:
        - sudo yum install docker-ce -y

    - name: Utils Install
      command: "{{ item }}"
      with_items:
        - sudo yum install -y ntp tar xz unzip curl ipset bind-utils
        - sudo sed -i s/SELINUX=enforcing/SELINUX=permissive/g /etc/selinux/config
        - sudo yum install haveged -y
        - sudo chkconfig haveged on

    - name: Firewalld off
      command: "{{ item }}"
      with_items:
        - sudo systemctl stop firewalld
        - sudo systemctl disable firewalld
      ignore_errors: True

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

- hosts: master:public:agent:gitlab
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
    - block:
      - name: Checking Docker folders
        stat:
         path: "{{item}}"
        register: folder_stats
        with_items:
        - ["/etc/docker/"]
      - name: Creating Docker folders
        become: true
        become_method: sudo
        file:
         path: "{{item.item}}"
         state: directory
         mode: 0644
         group: root
         owner: root
        when: item.stat.exists == false
        with_items:
        - "{{folder_stats.results}}"

    - name: Docker devicemapper
      become: true
      become_method: sudo
      copy:
        content: |
          {"storage-driver": "vfs","insecure-registries" : ["{{registry_host}}"]}
        dest: /etc/docker/daemon.json
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
# ansible-playbook ansible-playbook.yml --start-at-task="Docker devicemapper"

# play at task and step
# ansible-playbook ansible-playbook.yml --step --start-at-task="genconf"
```

#### playbook.yml 실행

다음의 명령어로 앤시블 프로비져닝을 실행합니다. 단계별로 실행을 원할 경우 --step 옵션을 추가하도록 합니다.

```
*주의사항 : 사전에 모든 노드는 known hosts 파일에 등록이 되어있어야 합니다.

노드에 ssh 접속을 한번 이상 했다면 자동으로 등록됩니다.
```

```
cd install
ansible-playbook ansible-playbook.yml
```

플레이북은 타스크들로 구성되어있으며, 만약 특정 타스크부터 수행하고 싶을 경우 다음의 명령어를 사용합니다.

```
cd install
ansible-playbook ansible-playbook.yml --start-at-task="Docker service file"
```

또는, 한가지의 타스크만 실행하고 싶을때는 다음의 명령어를 사용합니다.

```
cd install
ansible-playbook ansible-playbook.yml --step --start-at-task="Docker service file"
```

### 클러스터 설치

다음의 진행사항은 bootstrap 노드에서만 해당됩니다.

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

genconf/ssh_key 에 접속가능한 private key pem 파일의 내용을 넣고, 퍼미션을 400 으로 조정하세요.

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

모든 서버의 resolvers 가 동일하다고 가정해야 하며, 보통은 OS 의 /etc/resolv.conf 에 정의되어있습니다.
 
/etc/resolv.conf 참조하여 만약 다음의 값이라면

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

/etc/resolv.conf 에 search 가 없다면, genconf/config.yaml 파일에 dns_search 값은 없어도 됩니다. 


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
sudo su
dcos-shell /opt/mesosphere/bin/dcos_add_user.py darkgodarkgo@gmail.com

export DCOS_USER=darkgodarkgo@gmail.com
DCOS_ACS_TOKEN="$(docker run --rm -v /var/lib/dcos/dcos-oauth/auth-token-secret:/key karlkfi/jwt-encoder ${DCOS_USER} /key --duration=86400000)"
echo $DCOS_ACS_TOKEN

# 토큰 테스트
curl --header "Authorization: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiJkYXJrZ29kYXJrZ29AZ21haWwuY29tIiwiZXhwIjozMTI1NTI2NDE5MzYuMjc2M30.IoIANXtSpc000tNFdpDGIC5MlbezSD7ovnellaJMOOs" http://localhost/service/marathon/v2/apps
```

획득한 토큰은 깃랩 데이터베이스 설치에 필요하니 기억하고 있도록 합니다.


#### CLI 설치

부트스트랩 노드에서, 다음을 실행합니다.

```
curl -O https://downloads.dcos.io/binaries/cli/linux/x86-64/dcos-1.10/dcos
sudo mv dcos /usr/local/bin
chmod +x /usr/local/bin/dcos

# 마스터 중 하나로 지정합니다.
# CLI 클라이언트는 사용자 등록을 위해 웹브라우저 주소를 리다이렉트 합니다. 그러므로, 마스터 노드의 퍼블릭 아이피를 넣어주도록 합니다.
# 네트워크 환경이 프라이빗 아이피로도 마스터 노드로의 브라우저 접속이 가능하다면, 프라이빗 아이피 그대로 사용하셔도 무방합니다.
dcos cluster setup http://192.168.0.14

If your browser didn't open, please go to the following link:

    http://192.168.0.14/login?redirect_uri=urn:ietf:wg:oauth:2.0:oob
Enter OpenID Connect ID Token: 
```

콘솔의 url 로 이동하여, 화면에 나오는 토큰값을 대화창에 입력하면 CLI 를 사용가능 합니다.


### Gitlab

다음은 깃랩 서버 인스톨입니다.

#### 도커 레지스트리 서버 준비

```
sudo docker run -d -p 5000:5000 --restart=always --name registry registry:2
```

#### 깃랩 인스톨

<Gitlab>

```
sudo yum install -y curl policycoreutils-python openssh-server
sudo systemctl enable sshd
sudo systemctl start sshd
sudo firewall-cmd --permanent --add-service=http
sudo systemctl reload firewalld

curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.rpm.sh | sudo bash

sudo EXTERNAL_URL="http://gitlab.example.com" yum install -y gitlab-ce-10.2.4-ce.0.el7.x86_64

```

<Gitlab CI Runner>


```
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh | sudo bash

sudo yum install gitlab-runner -y

```

<Gitlal Configuration>

```
sudo vi /etc/gitlab/gitlab.rb

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

sudo gitlab-ctl reconfigure
```

TODO - <Gitlab private 토큰 발급받기>

스샷 있음.

<Gitlab CI Runner 등록>

```
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

npm install npm@latest -g
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

cd template-zuul
git init
git remote add origin http://gitlab.pas-mini.io/root/template-zuul.git
git add .
git commit -m "Initial commit"
git push -u origin master

cd template-iam
git init
git remote add origin http://gitlab.pas-mini.io/root/template-iam.git
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

#### 클라우드 패키지 파일 수정 

다음의 파일 리스트들의 값을 변경하도록 합니다.

- uengine-cloud-config/src/main/resources/application.yml

```
spring:
  cloud:
    config:
      server:
        git:
          uri: http://gitlab.pas-mini.io/root/cloud-config-repository.git
          username: root
          password: adminadmin
```

- uengine-cloud-server/src/main/resources/bootstrap.yml

```
spring:
  application:
      name: uengine-cloud-server
  profiles:
    active: "dev"

---
spring:
  profiles: dev
  cloud:
    config:
      uri: http://config.pas-mini.io

---
spring:
  profiles: docker
  cloud:
    config:
      uri: http://marathon-lb-internal.marathon.mesos:10000

```

- uengine-cloud-ui/index.html

```
.
.
  <script>
    var configServerUrl = 'http://config.pas-mini.io';
    var baseUrl = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '');
  </script>
.
.
```


#### 클라우드 패키지 배포 파일 수정

- uengine-cloud-config/deploy.json

```
{
.
.
"image": "gitlab.pas-mini.io:5000/uengine-cloud-config:v1",
.
.
"HAPROXY_0_VHOST": "config.pas-mini.io",
.
.
}
```

- uengine-cloud-server/deploy.json

```
{
.
.  
"image": "gitlab.pas-mini.io:5000/uengine-cloud-server:v1",
.
.  
"HAPROXY_0_VHOST": "cloud-server.pas-mini.io"
.
.
}
```

- uengine-cloud-ui/deploy.json

```
{
.
.
"image": "gitlab.pas-mini.io:5000/uengine-cloud-ui:v1",
.
.
"HAPROXY_0_VHOST": "cloud.pas-mini.io",
.
.
}

```

- uengine-eureka-server/deploy.json

```
.
.
"image": "gitlab.pas-mini.io:5000/uengine-eureka-server:v1",
.
.
"HAPROXY_0_VHOST": "eureka-server.pas-mini.io"
.
.
}
```


#### 클라우드 패키지 빌드 및 도커 파일 생성

```
cd install
sh docker-build.sh
```

#### 클라우드 패키지 실행

```
cd install

-- 클라우드 콘피스 서버와 유레카 서버를 구동시킨다.

dcos marathon app add ../uengine-cloud-config/deploy.json
dcos marathon app add ../uengine-eureka-server/deploy.json


-- 데이터베이스를 구동시킨다.

dcos marathon app add ../db/deploy.json


-- 위의 항목들이 모두 구동완료되었을 때 나머지를 실행시킨다.

dcos marathon app add ../uengine-cloud-iam/deploy.json
dcos marathon app add ../uengine-cloud-server/deploy.json
dcos marathon app add ../uengine-cloud-ui/deploy.json
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




