# 설치파일 다운로드 && 유틸리티 준비

하기의 모든 사항은, bootstrap 노드에서 진행하도록 합니다.

- 서버에 git, wget 이 있는지 확인하고, 없다면 다음 명령어로 설치합니다.

```
sudo yum install git wget -y
```

## Java 설치

[오라클 자바 다운로드](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 페이지로 이동합니다.

Accept License Agreement 를 클릭하신 후, jdk-8u161-linux-x64.tar.gz 의 링크를 복사합니다.

wget 명령어로 다운받은 후, /opt/ 폴더로 이동시킵니다.

```
# wget --header "Cookie: oraclelicense=accept-securebackup-cookie" <link>
# sudo mv jdk-8u161-linux-x64.tar.gz /opt/
```

이후 설치를 진행합니다.

```
# sudo su
# cd /opt/
# tar xzf jdk-8u161-linux-x64.tar.gz

# cd /opt/jdk1.8.0_161/
# alternatives --install /usr/bin/java java /opt/jdk1.8.0_161/bin/java 2
# alternatives --config java

2 개의 프로그램이 'java'를 제공합니다.

  선택    명령
-----------------------------------------------
*+ 1           java-1.8.0-openjdk.x86_64 (/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.151-5.b12.el7_4.x86_64/jre/bin/java)
   2           /opt/jdk1.8.0_161/bin/java

현재 선택[+]을 유지하려면 엔터키를 누르고, 아니면 선택 번호를 입력하십시오:2

alternatives --install /usr/bin/jar jar /opt/jdk1.8.0_161/bin/jar 2
alternatives --install /usr/bin/javac javac /opt/jdk1.8.0_161/bin/javac 2
alternatives --set jar /opt/jdk1.8.0_161/bin/jar
alternatives --set javac /opt/jdk1.8.0_161/bin/javac


# vi /etc/profile

파일의 제일 하단 부분에 다음을 추가합니다.

export JAVA_HOME=/opt/jdk1.8.0_161
export JRE_HOME=/opt/jdk1.8.0_161/jre
export PATH=$PATH:/opt/jdk1.8.0_161/bin:/opt/jdk1.8.0_161/jre/bin

# source /etc/profile
```

## Maven 설치

```
# sudo su
# wget http://apache.mirror.cdnetworks.com/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
# tar xvf apache-maven-3.3.9-bin.tar.gz
# mv apache-maven-3.3.9  /usr/local/apache-maven

# vi /etc/profile

파일의 제일 하단 부분에 다음을 추가합니다.

export M2_HOME=/usr/local/apache-maven
export M2=$M2_HOME/bin
export PATH=$M2:$PATH

# source /etc/profile
# mvn -version
```

## Node 설치

```
# sudo su
# curl --silent --location https://rpm.nodesource.com/setup_7.x | bash -
# yum install nodejs -y

# npm install npm@latest -g
# node -v
# npm -v

# (For phantomJs install)
# yum install bzip2 -y
```

## ansible 설치

```
sudo yum install epel-release -y
sudo yum install ansible -y
```



