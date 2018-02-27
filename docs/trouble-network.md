# 트러블 슈팅 - 네트워크

유엔진 클라우드 플랫폼의 앱들의 최종 라우팅은 Haproxy 로써, DC/OS 에서 제공하는 marathon-lb 오픈소스입니다. 
앱 생성 후 라우터 접속이 원할하지 않은 경우, Haproxy 의 설정 및 네트워크 상황을 다음의 레퍼런스를 통하여 체크할 수 있습니다.

| Statistics                | <public-node>:9090/haproxy?stats         |
|---------------------------|------------------------------------------|
| Statistics CSV            | <public-node>:9090/haproxy?stats;csv     |
| Health check              | <public-node>:9090/_haproxy_health_check |
| Configuration file view   | <public-node>:9090/_haproxy_getconfig    |
| Get vHost to backend map  | <public-node>:9090/_haproxy_getvhostmap  |
| Get app ID to backend map | <public-node>:9090/_haproxy_getappmap    |
| Reload configuration      | <public-node>:9090/_mlb_signal/hup*      |