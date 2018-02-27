# 디스크 마운트(옵셔널)

## 로그 디렉토리 마운트

- 마스터 서버는 많은 양의 로그가 발생하게 되므로, /var/log 에 충분한 디스크 공간을 할당하길 바랍니다.
- 슬레이브 노드는 도커가 실행되게 되므로, /var/lib/docker 에 충분한 디스크 공간을 할당하길 바랍니다.

## 디스크 마운트

* OS 가 이미 충분한 양의 디스크에서 운용이 되고있다면 이 단계는 넘어가도 됩니다. 
* 하지만 100GB 이하의 디스크 볼륨에서 OS 가 운용되고있거나, 장기적인 운용을 생각한다면 각 서버의 데이터 디렉토리에 대해 별도의 볼륨 디스크를 활용하시길 권장합니다.

모든 서버에 100GB 이상의 볼륨을 생성했다고 가정합니다. 생성한 볼륨을 서버별로 다음의 디렉토리에 마운트를 수행하여야 합니다.

- Master : /var/log
- Slave : /var/lib/docker

다음 명령어로 마운트를 수행할 수 있습니다.

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
만일 가상서버에 디스크 할당을 하였음에도 볼륨을 확인할 수 없을 경우는 첨부: 파티션 확인,파티션 추가를 참조하세요.

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


* 첨부: 파티션 확인

```
# lsblk

sda               8:0    0   20G  0 disk 
├─sda1            8:1    0  500M  0 part /boot
├─sda2            8:2    0  9.5G  0 part 
│ ├─centos-root 253:0    0  8.5G  0 lvm  /
│ └─centos-swap 253:1    0    1G  0 lvm  [SWAP]
sr0              11:0    1  366K  0 rom  
```


* 첨부: 파티션 추가

sda 하위에 파티션을 추가할 경우

```
# sudo su
# fdisk /dev/sda

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
