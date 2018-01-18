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