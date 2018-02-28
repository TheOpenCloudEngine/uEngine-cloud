# 자체 IAM 연동하기

- IAM Server 만들기
1. App 생성 > IAM Server 에서 IAM 을 생성한다. 
1. 생성한 IAM 서버를 접속해본다.
1. 유저를 추가한다.
1. 로그인 창을 접속해본다.
1. 로그인 창의 CI와 디자인을 우리 회사에 맞게 수정한다. (Login.vue 파일)

- 내 프론트에 IAM 연동
* router/index.js 에서 Login 컴포넌트에 대한 프로퍼티 수정으로 새로 만든 IAM 서버에 접속이 가능하다.
```js
      component: Login,
      props: {
        "iamServer": "http://my-iam-dev.pas-mini.io/",  //생성한 iam server 주소
        "scopes": "catalog-access"   // 요청 scope list
      },
```


# Zuul 로 진입점 통일 (장진영)
# hateoas 통일 links (장진영)