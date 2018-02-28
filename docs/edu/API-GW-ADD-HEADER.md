예시: OCE APIGW 를 통과한 유저에게 별도 인증 없이 깃랩 API를 사용할 수 있도록 함
```
    gitlab:
      url: http://gitlab.pas-mini.io/api/v4/
      stripPrefix: true
      addHeader:
        PRIVATE-TOKEN: EBXSvH....3MzB2sc9Qz
```

예시: OCE IAM 의 권한을 확인하여 개발자 권한 (developer)가 있으면 gitlab 접근 권한 체크와 함께 토큰을 전달한다
```
    gitlab:
      url: http://gitlab.pas-mini.io/api/v4/
      stripPrefix: true
      iam-scopes:
        - developer/*
      addHeader:
        PRIVATE-TOKEN: ${iam.user.gitlab_token}
```

예시: OCE APIGW 를 통과한 유저별 저장된 토큰을 얻어서 포워드 함 (to-be)
```
    gitlab:
      url: http://gitlab.pas-mini.io/api/v4/
      stripPrefix: true
      addHeader:
        PRIVATE-TOKEN: ${iam.user.gitlab_token}
```