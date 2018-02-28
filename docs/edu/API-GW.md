# 아키텍처
1. Netflix Zuul을 확장
1. OCE DevOps 포탈을 통한 (GUI 기반) 설정과 무정지 반영

# 기능
1. 보안 
- CORS 설정
- [권한 처리 (OCE-IAM 과 연동)](edu/API-GW-SECURITY)

1. 통합
- [원천 서비스에 대한 헤더 삽입](edu/API-GW-ADD-HEADER)
- 룰 기반 인바운드 메시지의 트랜스포메이션 (uEngine5 BPM 과 통합)

1. 성능
- Fallback
- DDoS 공격 식별 서비스 보호
