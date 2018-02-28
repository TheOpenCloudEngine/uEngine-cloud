- IAM 에 정의된 보안권한에 따라 요청된 유저의 권한에 따른 서비스 각 액션(메서드)의 요청을 열거나 닫는다.
- 예제 1:
```
    order:
      path: /orders/**
      serviceId: order-service
      stripPrefix: false

      # 일반 게스트 유저에 대해서는 (GET) 허용, catalog-order scope 을 가진 유저인 경우 PUT, PATCH, POST, DELETE 허용
      iam-scopes:
        - guest/GET
        - catalog-order/PUT-PATCH-POST-DELETE
```

- 예제 2: catalog-user scope 을 보유한 유저는 모든 (*) 액션 허용
```
    item:
      path: /items/**
      serviceId: order-service
      stripPrefix: false
      iam-user-scopes-check: false
      iam-scopes:
        - catalog-user/*
```