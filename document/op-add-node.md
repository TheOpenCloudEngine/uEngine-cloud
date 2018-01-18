# 노드 추가

이 파트는 **private-agent** 를 추가하는 작업을 기술합니다. **master** 노드를 추가 / 삭제하는 것은 
기존 서비스를 중단하지 않고 수행하는 것은 매우 어려운 절차를 가지므로 플랫폼 배포사에 문의를 하시기 바랍니다.

`uEngine-cloud/config.yml` 파일에 `add-agent` 항목에, 추가할 노드 리스트를 기술합니다.

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

`uEngine-cloud/config.yml` 을 작성 후, 빌드를 실행합니다. 이 단계를 실행하면 DC/OS 콘피그레이션 및 앤서블 스크립트가 재생성됩니다.

```
$ cd uengine-resource
$ mvn clean install exec:java package
```

이후, `uEngine-cloud/install` 폴더에서 변경 적용된 DC/OS 클러스터 배포파일을 생성 후, 압축합니다.

```
$ cd install
$ sudo bash dcos_generate_config.sh --genconf
$ cd genconf/serve 
sudo tar cf dcos-install.tar *
```

다시 `uEngine-cloud/install` 폴더로 돌아와, 마지막으로 `ansible-add-agent.yml` 플레이북을 실행합니다.

```
$ cd install
$ ansible-playbook ansible-add-agent.yml
```

