
# MSA 개요
1. https://www.slideshare.net/pongsor/micro-service-architecture-84941530

# Open Cloud Engine 개발자 레퍼런스
1. [OCE API GW](edu/API-GW)
1. [OCE IAM](edu/OCE-IAM)
1. [미터링 / 빌링](https://github.com/TheOpenCloudEngine/uEngine-bill)
1. [OCE BPM](https://github.com/TheOpenCloudEngine/uEngine5-base)

# 예제 애플리케이션 소개

![App](https://user-images.githubusercontent.com/16382067/35211738-0f4d4f16-ff9b-11e7-9c0a-dfb2d31ff22b.png)

1. 실 구현 세부 그림
![image](https://user-images.githubusercontent.com/16382067/35252243-de646b7c-0022-11e8-95e5-8b92f159a44e.png)

1. [마이크로 서비스 구현 전략](edu/구현-전략)

# OCE MSA 플랫폼 사용
1. [계정 생성과 클라우드 네이티브 애플리케이션 생성](edu/OCE-MSA-플랫폼의-사용.md)
1. [관련 도구 설치](edu/Httpie-설치)

# 도메인 서비스의 구현 (장진영 작업)
1. [주문/재고서비스](edu/주문서비스의-구현.md) 
1. [고객서비스](edu/고객서비스의-구현.md)

# 수준 높은 마이크로 서비스
1. [마이크로 서비스의 분리](edu/마이크로-서비스의-분리)
1. [API Gateway 통한 Host 통합과 보안 처리](edu/API-Gateway)
1. [멀티테넌시 처리](edu/멀티테넌시)
1. [IAM/API Gateway 을 이용한 멀티테넌시와 기능별 접근 제한](edu/IAM-API-GW-통한-멀티테넌시와-접근제어)
1. [마이크로 서비스간 통신](edu/마이크로서비스-간-커뮤니케이션)



# 컨슈머 만들기 1 - Web UI 만들기
1. [VueJS](https://github.com/TheOpenCloudEngine/micro-service-architecture-vuejs/wiki/Vue-JS-Basics)
1. [Web UI 만들기](edu/Web-UI-만들기)
1. [IAM 연동](edu/IAM-연동)

# 컨슈머 만들기 2 - BPM 통한 orchestration 
1. BPM 서비스의 디플로이
    1. Process Service
    1. Definition Service
1. BPM 서비스 접속
    1. 통합 계정 사용시
    1. 별도 계정 사용시
1. 기본 기능 테스트
    1. 프로세스 작성
    1. 프로세스 실행
    1. 프로세스 모니터링
1. 서비스 통합 프로세스의 구현
    1. 프로세스 개요
    1. 풀과 서비스 태스크
    1. SOA Maturity Model 

# 컨슈머 만들기 3 - 챗봇 시나리오 (김상훈 작업)
1. [챗봇 시나리오](edu/챗봇-시나리오)
1. [카카오톡과 연동](edu/카카오톡과-연동)
    
# Production 과 무정지 운영 (박승필 작업)
1. canary 디플로이
1. 모니터링과 대응
    1. 인스턴스별 
    1. 앱별
    1. 병목지점 발견
    1. 개선
1. 오토 스케일링

# 확장주제 (장진영 작업)
1. 트랜잭션관리
    1. CQRS
    1. Event Sourcing
1. 서비스 보호
    1. DDOS 공격
    1. Circuit Breaker / Fallback
1. 미터링 / 빌링

