# Virtual Box CentOS7

### 이미지 다운로드

[https://www.centos.org/download/](https://www.centos.org/download/) 에서 Minimal ISO 를 다운받습니다.

### 설치

Virtual Box 를 실행시킨 후, 다음 그림순서대로 설치를 진행하십시오.

--
![](image/virual1.png)

--
![](image/virual2.png)

--
![](image/virual3.png)

--
![](image/virual4.png)

--
![](image/virual5.png)

--
![](image/virual6.png)

--
![](image/virual7.png)

--
![](image/virual8.png)

--
![](image/virual9.png)

--
![](image/virual10.png)

--
![](image/virual11.png)


### centos 유저 sudo 등록

다음은 centos 유저에 대해 sudo 실행시 패스워드 프롬프트를 생략하도록 설정합니다.

`/etc/sudoers` 퍼미션을 편집가능하게 변경하고, 아래의 내용을 찾아 변경하도록 합니다.

```
$ chmod u+w /etc/sudoers
$ vi /etc/sudoers

.
.
## Allow root to run any commands anywhere
root    ALL=(ALL)       ALL
centos ALL=(ALL) NOPASSWD: ALL

## Allows members of the 'sys' group to run networking, software,
## service management apps and more.
# %sys ALL = NETWORKING, SOFTWARE, SERVICES, STORAGE, DELEGATING, PROCESSES, LOCATE, DRIVERS

## Allows people in group wheel to run all commands
#%wheel ALL=(ALL)       ALL

## Same thing without a password
%wheel  ALL=(ALL)       NOPASSWD: ALL
.
.
```

`/etc/sudoers` 퍼미션을 원복합니다.

```
$ chmod u-w /etc/sudoers
```