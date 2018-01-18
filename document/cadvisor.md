# Cadvisor Proxy Server
 
[클러스터 설치](install-cluster.md) 과정에서 Google 의 `cadvisor` 도커가 함게 설치되게 되는데, 이는 도커 컨테이너 메트릭스를 
수집하여 APM , 모니터링 등에 이용할 수 있게 해주는 툴 입니다.

cadvisor 는 각 에이전트 노드마다 설치되는데, 유엔진 클라우드 플랫폼은 에이전트 서버로의 직접적인 접근을 허용하지 않기 때문에 `public-agent` 
노드로 cadvisor 정보를 proxy 통신을 시켜주어야 합니다.

`public-agent` 노드로 이동한 후, 다음 설치과정을 진행합니다.

```
$ ssh -i <your-private-key> public
```

## rinted 설치

```
$ sudo yum -y install gcc
$ wget http://www.boutell.com/rinetd/http/rinetd.tar.gz
$ tar zxvf rinetd.tar.gz
$ cd rinetd
$ sudo make
$ sudo mv rinetd /usr/sbin/
$ sudo chcon -u system_u -t bin_t /usr/sbin/rinetd
```

## rinted 프록시 설정

`cadvisor` 서비스는 각 agent-node 의 8080 포트를 통해 서비스 됩니다. 모든 에이전트 노드의 8080 서버들이 `public-agent` 의 90** 포트로 프록시 
통신이 되도록 설정합니다. 예를 들어, agent11 번인 경우, 9011 포트로 설정합니다.

```
$ sudo vi /etc/rinetd.conf

logfile /var/log/rinetd/rinetd.log
logcommon

#src IP, Port, dest IP, Port
0.0.0.0 9100 172.31.5.136 8080  # public agent is 9100
0.0.0.0 9101 172.31.6.35 8080   # other agent is 91** (agent number)
0.0.0.0 9102 172.31.1.235 8080
0.0.0.0 9103 172.31.5.245 8080
0.0.0.0 9104 172.31.14.247 8080
0.0.0.0 9105 172.31.7.160 8080
0.0.0.0 9106 172.31.11.70 8080
0.0.0.0 9107 172.31.0.164 8080
```

## rinted 서비스 등록

`/etc/rc.d/init.d/rinetd` 생성

```
$ sudo vi /etc/rc.d/init.d/rinetd

#!/bin/sh
#
# chkconfig: - 80 20
# description: rinetd is a TCP redirection server
# processname: rinetd
# pidfile: /var/run/rinetd.pid
# config: /etc/rinetd.conf

# Source function library.
. /etc/rc.d/init.d/functions

# Source networking configuration.
. /etc/sysconfig/network

# Check that networking is up.
[ ${NETWORKING} = "no" ] && exit 1

exec="/usr/sbin/rinetd"
prog=$(basename $exec)

lockfile=/var/lock/subsys/$prog

start() {
    echo -n $"Starting $prog: "
  daemon $exec
    retval=$?
    echo
    [ $retval -eq 0 ] && touch $lockfile
    return $retval
}

stop() {
    echo -n $"Stopping $prog: "
    killproc $prog
    retval=$?
    echo
    [ $retval -eq 0 ] && rm -f $lockfile
    return $retval
}

restart() {
    stop
    start
}

reload() {
  if [ -f "$lockfile" ]; then
    echo -n $"Reloading $prog: "
    killproc $prog -HUP
    retval=$?
    echo
    return $retval
  else
    restart
  fi
}

force_reload() {
    restart
}

fdr_status() {
    status $prog
}

case "$1" in
    start|stop|restart|reload)
        $1
        ;;
    force-reload)
        force_reload
        ;;
    status)
        fdr_status
        ;;
    condrestart|try-restart)
    [ ! -f $lockfile ] || restart
  ;;
    *)
        echo $"Usage: $0 {start|stop|status|restart|try-restart|reload|force-reload}"
        exit 2
esac
```

서비스 등록

```
$ sudo chcon -u system_u -t initrc_exec_t /etc/rc.d/init.d/rinetd
$ sudo chmod +x /etc/rc.d/init.d/rinetd
$ sudo chkconfig rinetd on
```

`/etc/logrotate.d/rinetd` 로그 파일 생성

```
$ sudo vi /etc/logrotate.d/rinetd

/var/log/rinetd.log {
  missingok
  notifempty
  delaycompress
  postrotate
    /sbin/service rinetd restart 2> /dev/null > /dev/null || true
  endscript
}
```

`rinetd` 서비스 최종 실행

```
$ sudo mkdir /var/log/rinetd
$ sudo chcon -u system_u /var/log/rinetd
$ sudo service rinetd start
```


이후, 깃랩 UI 의 `cloud-config-repository` 프로젝트의 `uengine-cloud-server.yml` 
파일에 등록한 노드만큼 `<추가아이피>,http://<퍼블릭노드아이피>:91**` 형식으로 더해줍니다. 

```
cadvisor:
  - 172.31.5.136,http://52.79.51.79:9100
  - 172.31.6.35,http://52.79.51.79:9101
  - 172.31.1.235,http://52.79.51.79:9102
  - 172.31.5.245,http://52.79.51.79:9103
  - 172.31.14.247,http://52.79.51.79:9104
  - 172.31.7.160,http://52.79.51.79:9105
  - 172.31.11.70,http://52.79.51.79:9106
  - 172.31.0.164,http://52.79.51.79:9107
```

모든 과정이 종료된 후, 클라우드 플랫폼에서 각 앱의 타스크(컨테이너) 상세보기를 통해 cadvisor 메트릭스를 볼 수 있습니다.