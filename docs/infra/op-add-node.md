# 노드 추가

이 파트는 **private-agent** 를 추가하는 작업을 기술합니다. **master** 노드를 추가 / 삭제하는 것은 
기존 서비스를 중단하지 않고 수행하는 것은 매우 어려운 절차를 가지므로 플랫폼 배포사에 문의를 하시기 바랍니다.

`uEngine-cloud/uengine-resource/config.yml` 파일에 `add-agent` 항목에, 추가할 노드 리스트를 기술합니다.

```
$ vi uEngine-cloud/config.yml

.
.
    add-agent:
      agent8: 172.31.10.202
      agent9: 172.31.14.197
      agent10: 172.31.13.131
      agent11: 172.31.1.166
      agent12: 172.31.8.5 
```

`uEngine-cloud/uengine-resource/config.yml` 을 작성 후, 빌드를 실행합니다. 이 단계를 실행하면 DC/OS 콘피그레이션 및 앤서블 스크립트가 재생성됩니다.

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

이후, `uEngine-cloud/install` 폴더에서 변경 적용된 DC/OS 클러스터 배포파일을 생성 후, 압축합니다.

```
# 앤시블 호스트 파일 재적용
$ cd install
$ sudo sh -c "cat ansible-hosts.yml > /etc/ansible/hosts"

# DC/OS 생성파일 생성
$ sudo bash dcos_generate_config.sh --genconf

# DC/OS 배포파일 압축
$ cd genconf/serve 
sudo tar cf dcos-install.tar *
```

다시 `uEngine-cloud/install` 폴더로 돌아와, 마지막으로 `ansible-add-agent.yml` 플레이북을 실행합니다.

```
$ cd install
$ ansible-playbook ansible-add-agent.yml
```

## cadvisor 프록시 추가

Google cadvisor 서비스를 사용하기 위해 public node 의 `rinted` 프록시 서버를 설정합니다.

추가하는 순서는 agent 번호 순번대로, 91** 으로 만들어주면 됩니다. 예를들어 agent12 번일 경우, 9112 포트가 됩니다.

```
$ ssh -i <your-private-key> public

$ sudo vi /etc/rinetd.conf

.
.
0.0.0.0 9108 172.31.10.202 8080
0.0.0.0 9109 172.31.14.197 8080
0.0.0.0 9110 172.31.13.131 8080
0.0.0.0 9111 172.31.1.166 8080
0.0.0.0 9112 172.31.8.5 8080
```

`rinted` 프록시 서버를 재시작합니다.

```
$ sudo service rinetd restart
Restarting rinetd (via systemctl):                         [  OK  ]
```

이후, 깃랩 UI 의 `cloud-config-repository` 프로젝트의 `uengine-cloud-server.yml` 
파일에 추가된 노드만큼 `<추가아이피>,http://<퍼블릭노드아이피>:91**` 형식으로 더해줍니다. 

![token](infra/image/add-agent1.png)

```
cadvisor:
  .
  .
  .
  - 172.31.10.202,http://52.79.51.79:9108
  - 172.31.14.197,http://52.79.51.79:9109
  - 172.31.13.131,http://52.79.51.79:9110
  - 172.31.1.166,http://52.79.51.79:9111
  - 172.31.8.5,http://52.79.51.79:9112
```








