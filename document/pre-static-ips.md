## 네트워크 고정 아이피(옵셔널)

* 네트워크 전용선 과 허브를 구비한 사업장은 이 섹션을 넘어가도 됩니다.
* 퍼블릭 이아스 서비스(AWS 등)을 사용하는 사업장 또한 이 섹션을 넘어가도 됩니다.

유엔진 클라우드 플랫폼은 설치시 지정된 서버들의 프라이빗 아이피가 변경되어서는 안됩니다.
이유는 마스터 노드간의 통신에 필요한 주키퍼 설정이 아이피로 정해지기 때문인데, 
아주 많은 컴포넌트들이 이 설정을 이용하므로 설치 후 프라이빗 아이피를 바꾸는 일은 매우 어렵습니다.
  
그러므로, 만약 이 플랫폼을 가정 또는 교육용으로 설치하실 경우 다음을 체크하세요.
 
- 클러스터를 구성하는 서버들을 가상화 프로그램을 이용한다. (버츄얼 박스 등)
- 가상화서버(VM) 네트워크를 DHCP 서버(공유기) 에서 할당받는다.
- 가상화서버(VM) 네트워크를 내부적으로 고정아이피를 사용하도록 하는 방법을 모르겠다.


위의 해당사항에 모두 해당될 경우, 가상서버를 재시작하거나 공유기가 재부팅 된 경우에 가상서버에 부여되는 아이피 주소가 바뀌게 됩니다.
이를 막기 위해서, 모든 서버에서 고정아이피를 지정하기 위해 아래의 절차를 따르도록 하세요.


1. 네트워크 스크립트 파일을 찾습니다.

```
경로 이동
# cd /etc/sysconfig/network-scripts

하위파일 탐색
# ll

-rw-r--r--. 1 root root   126  4월  3  2017 ifcfg-eth0
-rw-r--r--. 1 root root   254  5월  3  2017 ifcfg-lo
lrwxrwxrwx. 1 root root    24  1월 14 04:16 ifdown -> ../../../usr/sbin/ifdown
-rwxr-xr-x. 1 root root  1621  8월  3 07:12 ifdown-Team
-rwxr-xr-x. 1 root root  1556  4월 15  2016 ifdown-TeamPort
-rwxr-xr-x. 1 root root   654  5월  3  2017 ifdown-bnep
-rwxr-xr-x. 1 root root  6571  5월  3  2017 ifdown-eth
-rwxr-xr-x. 1 root root  6190  8월  4 10:21 ifdown-ib
-rwxr-xr-x. 1 root root   781  5월  3  2017 ifdown-ippp
-rwxr-xr-x. 1 root root  4540  5월  3  2017 ifdown-ipv6
lrwxrwxrwx. 1 root root    11  1월 14 04:16 ifdown-isdn -> ifdown-ippp
-rwxr-xr-x. 1 root root  1768  5월  3  2017 ifdown-post
-rwxr-xr-x. 1 root root  1068  5월  3  2017 ifdown-ppp
-rwxr-xr-x. 1 root root   870  5월  3  2017 ifdown-routes
-rwxr-xr-x. 1 root root  1456  5월  3  2017 ifdown-sit
-rwxr-xr-x. 1 root root  1462  5월  3  2017 ifdown-tunnel
lrwxrwxrwx. 1 root root    22  1월 14 04:16 ifup -> ../../../usr/sbin/ifup
-rwxr-xr-x. 1 root root  1755  4월 15  2016 ifup-Team
-rwxr-xr-x. 1 root root  1876  4월 15  2016 ifup-TeamPort
-rwxr-xr-x. 1 root root 12312  5월  3  2017 ifup-aliases
-rwxr-xr-x. 1 root root   910  5월  3  2017 ifup-bnep
-rwxr-xr-x. 1 root root 12680  5월  3  2017 ifup-eth
-rwxr-xr-x. 1 root root 10114  8월  4 10:21 ifup-ib
-rwxr-xr-x. 1 root root 12075  5월  3  2017 ifup-ippp
-rwxr-xr-x. 1 root root 11893  5월  3  2017 ifup-ipv6
lrwxrwxrwx. 1 root root     9  1월 14 04:16 ifup-isdn -> ifup-ippp
-rwxr-xr-x. 1 root root   650  5월  3  2017 ifup-plip
-rwxr-xr-x. 1 root root  1064  5월  3  2017 ifup-plusb
-rwxr-xr-x. 1 root root  3433  5월  3  2017 ifup-post
-rwxr-xr-x. 1 root root  4154  5월  3  2017 ifup-ppp
-rwxr-xr-x. 1 root root  2001  5월  3  2017 ifup-routes
-rwxr-xr-x. 1 root root  3303  5월  3  2017 ifup-sit
-rwxr-xr-x. 1 root root  2711  5월  3  2017 ifup-tunnel
-rwxr-xr-x. 1 root root  1836  5월  3  2017 ifup-wireless
-rwxr-xr-x. 1 root root  5419  5월  3  2017 init.ipv6-global
-rw-r--r--. 1 root root 18919  5월  3  2017 network-functions
-rw-r--r--. 1 root root 31027  5월  3  2017 network-functions-ipv6
```

2. 네트워크 스크립트 파일 수정

대부분 eth0 이나 eth1 이 메인 네트워크 장비로 잡혀있습니다. ifcfg-eth0 또는 ifcfg-eht1 스크립트를 수정하도록 합니다.

- IPADDR: 설정할 고정아이피
- GATEWAY: 참조-게이트웨이 아이피 보기
- DNS1,...: 참조-DNS 설정 보기

```
#vi ifcfg-eth0

TYPE=Ethernet
BOOTPROTO=static
DEVICE=eth0
ONBOOT=yes
IPADDR=192.168.0.8
NETMASK=255.255.255.0
GATEWAY=192.168.0.1
DNS1=121.88.255.50
DNS2=121.88.255.49

수정 후 네트워크 재시작
# service network restart
```

* 참조: 게이트웨이 아이피 보기

```
ip route | grep default
```

* 참조: DNS 설정 보기

```
nmcli dev show eth0

GENERAL.장치:                           eth0
GENERAL.유형:                           ethernet
GENERAL.하드웨어주소:                   52:54:00:A9:9B:21
GENERAL.MTU:                            1500
GENERAL.상태:                           100 (연결됨)
GENERAL.연결 :                          System eth0
GENERAL.CON-경로:                       /org/freedesktop/NetworkManager/ActiveConnection/1
WIRED-PROPERTIES.캐리어:                켜짐
IP4.주소[1]:                            192.168.0.8/24
IP4.게이트웨이:                         192.168.0.1
IP4.DNS[1]:                             121.88.255.50
IP4.DNS[2]:                             121.88.255.49
IP6.주소[1]:                            fe80::5054:ff:fea9:9b21/64
IP6.게이트웨이:                         --
```