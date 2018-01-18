# 트러블 슈팅 - 프로비져닝 후 동작 로그 확인

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

