# 노드 삭제

노드를 제거하기 위해서는, 운영 중인 앱이 안전하게 다른 노드로 이전되고 난 후 제거되어야 합니다. `gracefully-remove-agent` 가 진행되기 위해서,
 `uEngine-cloud/uengine-resource/config.yml` 파일에 `gracefully-remove-agent` 항목에, 제거할 노드 리스트를 기술합니다.
 
```
$ vi uEngine-cloud/config.yml

.
.
    gracefully-remove-agent:
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

이후, `uEngine-cloud/install` 폴더에서 변경 적용된 DC/OS 클러스터 배포파일을 생성합니다. 
([노드 추가](op-add-node.md)와 다르게, 압축 파일을 생성하지 않아도 됩니다.)

```
# 앤시블 호스트 파일 재적용
$ cd install
$ sudo sh -c "cat ansible-hosts.yml > /etc/ansible/hosts"
```

다시 `uEngine-cloud/install` 폴더로 돌아와, 마지막으로 `ansible-gracefully-remove-agent.yml` 플레이북을 실행합니다.

```
$ cd install
$ ansible-playbook ansible-gracefully-remove-agent.yml
```

클라우드 플랫폼에서, 기존 타스크들이 남아있는 노드로 이동된 후, 노드가 삭제되는 모습을 볼 수 있습니다.
