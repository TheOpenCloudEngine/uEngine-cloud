# 설치 완전 삭제

설치를 완전히 제거하는 것은 다음과 같은 경우에 해당합니다.

 - 일부 `private-agent` 또는 `public-agent` 에 대하여 설치요소를 완전히 삭제할 경우
 - [클러스터 인스톨](infra/install-cluster.md) 과정에서 잘못된 설정값으로 초기 설치를 진행하여, 수정 후에도 실패를 경험하는 경우
 - 클러스터의 완전 삭제가 필요한 경우

노드 완전 삭제가 진행되기 위해서, `uEngine-cloud/uengine-resource/config.yml` 파일의 `uninstall` 항목에, 제거할 노드 리스트를 기술합니다.

```
$ vi uEngine-cloud/uengine-resource/config.yml

.
.
    uninstall:
      public: 172.31.5.136
      master1: 172.31.12.143
      master2: 172.31.4.125
      master3: 172.31.1.198
      agent1: 172.31.6.35
      agent2: 172.31.1.235
      agent3: 172.31.5.245
      agent4: 172.31.14.247
      agent5: 172.31.7.160
      agent6: 172.31.11.70
      agent7: 172.31.0.164
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
([노드 추가](infra/op-add-node.md)와 다르게, 압축 파일을 생성하지 않아도 됩니다.)

```
# 앤시블 호스트 파일 재적용
$ cd install
$ sudo sh -c "cat ansible-hosts.yml > /etc/ansible/hosts"
```

다시 `uEngine-cloud/install` 폴더로 돌아와, 마지막으로 `ansible-uninstall.yml` 플레이북을 실행합니다.

```
$ cd install
$ ansible-playbook ansible-uninstall.yml
```
