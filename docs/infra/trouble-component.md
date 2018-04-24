# 트러블 슈팅 - 컴포넌트 구동 확인

 - DC/OS [트러블 슈팅](https://dcos.io/docs/1.10/installing/troubleshooting/) 문서

아래는 DC/OS 를 구성하는 컴포넌트들의 로그를 볼 수 있는 명령어로, 컴포넌트의 동작상태를 tailing 합니다.

```
journalctl -flu dcos-exhibitor
journalctl -flu dcos-spartan
journalctl -flu dcos-mesos-dns
journalctl -flu dcos-mesos-slave
journalctl -flu dcos-mesos-slave-public
journalctl -flu dcos-mesos-master
journalctl -flu dcos-gen-resolvconf
```

## exhibitor - 주키퍼 정보 확인

http://<master-ip>/exhibitor

