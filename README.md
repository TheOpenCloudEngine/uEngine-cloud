# 사전 준비 사항

 — 모든 노드
 - 내부망 UDP 뚤려있을것: UDP 가 뚤려있지 않으면 53 포트 domain resolve 가 작동하지 않는다.
 - 내부망 TCP 포트는 모두 뚤려있을것
 - 외부망 인바운드 :  퍼블릭 에이전트 머신의 80 포트와 443 포트
 - 외부망 아웃바운드 : 모두 뚤려있을것
 - 네임서버 :  *.<server>.<domain>  형식으로, A MASK 가  *  로 설정된 도메인을 보유하고있을것.
 - 모든 서버는 동일한 pem 파일로 ssh 가 가능할 것.
 - 모든 서버의 /etc/ssh/ssh_config 는 PermitRootLogin 을 허용할 것.

# 설치파일 다운로드

git clone https://github.com/TheOpenCloudEngine/uEngine-cloud
wget https://s3.ap-northeast-2.amazonaws.com/uengine-cloud/dcos_generate_config.sh


# DCOS

## 호스트 준비

— RHEL

```
sudo vi /etc/sysconfig/network
HOSTNAME=192.168.0.7
sudo reboot
```

— CENTOS

```
sudo vi /etc/hostname
bootstrap
sudo hostname bootstrap
```

— 호스트 파일

```
sudo vi /etc/hosts
192.168.0.7  bootstrap
192.168.0.14 master1
192.168.0.23 master2
192.168.0.8 master3
192.168.0.30 public
192.168.0.27 agent1
192.168.0.28 agent2
192.168.0.24 agent3
192.168.0.25 agent4
192.168.0.31 agent5
192.168.0.26 agent6
192.168.0.33 agent7
```

— ssh config

```
sudo vi /etc/ssh/ssh_config
# PermitRootLogin no
sudo service sshd restart
```

## 클러스터 준비

### /etc/ansible/hosts

ansible-hosts-sample.yml 가이드 참조

```
cd uEngine-cloud

sudo yum install epel-release
sudo yum install ansible
sudo vi /etc/ansible/hosts

[all:vars]
ansible_user=centos
ansible_ssh_private_key_file=/home/centos/dcos-key.pem
registry_host=gitlab.pas-mini.io:5000

[bootstrap]
192.168.0.7

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

### playbook.yml

ansible-playbook-sample.yml 참조

```
# vi playbook.yml

---
- hosts: all
  remote_user: "{{ansible_user}}"
  tasks:
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
```

```
# play book
ansible-playbook playbook.yml

# play at task (IF failed, and restart from some task)
ansible-playbook playbook.yml --start-at-task="genconf"
```

## 클러스터 설치


### genconf

```
mkdir -p genconf

vi genconf/ip-detect

#!/usr/bin/env bash
set -o nounset -o errexit
export PATH=/usr/sbin:/usr/bin:$PATH
echo $(ip addr show eth0 | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' | head -1)
```

genconf/ssh_key 에 접속가능한 private key pem 파일 내용을 넣는다. 

```
vi genconf/ssh_key

<PRIVATE KEY>

chmod 400 genconf/ssh_key
```

config.yaml 생성한다.

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
- 192.168.0.14
- 192.168.0.23
- 192.168.0.8
process_timeout: 10000
public_agent_list:
- 192.168.0.30
resolvers:
- 121.88.255.50
- 121.88.255.49
ssh_key_path: genconf/ssh_key
ssh_port: 22
ssh_user: centos
telemetry_enabled: true
oauth_enabled: true
```

### resolvers, dns_search

/etc/resolve.conf 참조하여 만약 다음의 값이라면

```
search ap-northeast-2.compute.internal
nameserver 172.31.0.2
```

genconf/config.yaml 의 resolvers 는 nameserver 리스트로, dns_search 값은 search 값으로 설정한다.

```
.
.
resolvers:
- 172.31.0.2
dns_search: ap-northeast-2.compute.internal
```

### 설치

```
sudo bash dcos_generate_config.sh --genconf

sudo bash dcos_generate_config.sh --preflight

sudo bash dcos_generate_config.sh --deploy

sudo bash dcos_generate_config.sh --postflight
```

### 설치 후 동작 로그 확인

```
journalctl -flu dcos-exhibitor
journalctl -flu dcos-spartan
journalctl -flu dcos-mesos-dns
journalctl -flu dcos-mesos-slave
journalctl -flu dcos-mesos-slave-public
journalctl -flu dcos-mesos-master
journalctl -flu dcos-gen-resolvconf
```

### 사용자 생성

마스터 노드 중 한곳에서 실행한다.

```
sudo -i dcos-shell /opt/mesosphere/bin/dcos_add_user.py darkgodarkgo@gmail.com


export DCOS_USER=darkgodarkgo@gmail.com
DCOS_ACS_TOKEN="$(docker run --rm -v /var/lib/dcos/dcos-oauth/auth-token-secret:/key karlkfi/jwt-encoder ${DCOS_USER} /key --duration=86400000)"
echo $DCOS_ACS_TOKEN

# 토큰 테스트
curl --header "Authorization: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiJkYXJrZ29kYXJrZ29AZ21haWwuY29tIiwiZXhwIjozMTI1NTI2NDE5MzYuMjc2M30.IoIANXtSpc000tNFdpDGIC5MlbezSD7ovnellaJMOOs" http://localhost/service/marathon/v2/apps
```

획득한 토큰은 깃랩 데이터베이스 설치에 필요하니 기억하고 있도록 하자.


### CLI 설치

```
curl -O https://downloads.dcos.io/binaries/cli/linux/x86-64/dcos-1.10/dcos
sudo mv dcos /usr/local/bin
chmod +x /usr/local/bin/dcos

dcos cluster setup http://192.168.0.14

If your browser didn't open, please go to the following link:

    http://192.168.0.14/login?redirect_uri=urn:ietf:wg:oauth:2.0:oob
Enter OpenID Connect ID Token: 
```

콘솔의 url 로 이동하여, 화면에 나오는 토큰값을 대화창에 입력하면 CLI 를 사용가능하다.


## Gitlab

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

# 데이터베이스 설치

## Node 설치

```
sudo su
curl --silent --location https://rpm.nodesource.com/setup_7.x | bash -
yum install nodejs -y
node -v
npm -v
```

## Java && Maven 설치

### Java

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

### Maven

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

## 깃랩 데이터베이스 생성 - 추가 자동화 대상 TODO

## 데이터베이스 설정값 변경 - 추가 자동화 대상 TODO

## 데이터베이스 업로드

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

## 어플리케이션 실행

```
—— 마라톤 lb

dcos package describe --config marathon-lb

dcos package install marathon-lb

vi marathon-lb-internal.json

{ "marathon-lb":
   {"name":"marathon-lb-internal","haproxy-group":"internal","bind-http-https":false,"role":"",  "cpus": 1.0, "mem": 1024.0} 
}

dcos package install --options=marathon-lb-internal.json marathon-lb
```

이후 각각 0.5 , 512 MB 로 줄일것.

marathon-lb 는 특히 반드시 서스펜드 후 리소스 조정을 할것(host 서비스 특성)

