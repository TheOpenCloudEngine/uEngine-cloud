# REST Link 주소의 통합과 보안을 위한 API proxy

앞서 예제에서 다양한 마이크로서비스들이 각자의 주소로 link가 만들어지게 된다. 이는 여러분의 클라이언트 코드에서 직접적인 주소의 기입을 지양하고 link 에서 주어진 주소를 ajax 로 탐색하도록 구현하므로서 프론트엔드와 백엔드 간의 간섭을 최소화 해준다.  
이에 따라 얼마든지 주어진 주소의 host 가 바뀌더라도 웹브라우저는 알아서 여러개의 백엔드 마이크로서비스들을 알아서 방문할 것이다. 
하지만, 여러개의 백엔드를 웹브라우저가 접속하는데는 비용이 따라온다. 각 백엔드와 Cross Resource Sharing을 하기 위해서 백엔드 각자와 관계를 맺어야 한다. 물론, 완전 별개의 백엔드들을 통합할 때는 피할 수 없는 비용이지만 하나의 통합된 서비스내에 세부 마이크로 서비스 마다 백엔드 주소를 다르게 관리하기는 어렵다. 또한 마이크로서비스들을 외부 웹브라우저 수준에서 직접적으로 호출하게 하는 것은 보안상으로 위협이다. 
이러한 문제를 한번에 처리하기 위해서 API proxy 를 사용한다.

1. API Gateway Instance 의 생성
Order 서비스와 Customer 서비스를 묶어서 하나의 host 주소에서 사용할 수 있도록 해주는 측면에서 API Gateway 는 일종의 Composite Service 라 할 수 있다. OCE 에서는 API Gateway 또한 일종의 Spring Boot 기반 마이크로 서비스로 바라본다. 따라서 API Gateway 를 생성하고자 할때는 기존의 App 추가하는 방식과 동일하게 Apps 메뉴의 App 추가 버튼을 클릭하고 Zuul API Gateway 유형을 선택해주면 된다:

![image](https://user-images.githubusercontent.com/16382067/35032478-68651c04-fbaa-11e7-9854-c8ae93ebacde.png)

Zuul API 의 설정에서 다음 router 설정을 해준다:

- 앱이름: e-shop-api
- 외부 접속 주소(프로덕션): e-shop-api.pas-mini.io

서비스가 생성되고 자동으로 기동되는 동안, 런타임 및 환경 > Zuul 환경 설정에서 다음의 설정을 입력한다:

```
zuul:
  ignored-headers: Access-Control-Allow-Credentials, Access-Control-Allow-Origin
  addProxyHeaders: true
  routes:
    order:
      path: /orders/**
      serviceId: order-service
      stripPrefix: false
    customer:
      path: /customers/**
      serviceId: customer-service
      stripPrefix: false

```

이 설정으로 Zuul server 로 들어오는 orders/** 이하의 URI 리퀘스트는 order-service 로, customers/** 이하의 URI 요청은 customer-service 로 포워드하게 된다. 

설정을 마친후, 저장버튼을 클릭해주고, 상단의 애플리케이션 재기동 버튼 (리프래시 버튼)을 클릭하면 반영된다.
[주의] 이때 order-service 와 customer-service 는 소스코드를 커밋해주어 미리 개발기 서버상에 서비스가 올라와 있도록 있도록 해준다. 


1. 마이크로 서비스들의 host 주소 통일
이제 해당 서비스로 host 주소가 통일되었으니, 이를 기반으로 주문 리퀘스트를 보내보자:

```
http http://e-shop-api-dev.pas-min.io/customers firstName="jinyoung"
```

```
$ http http://e-shop-api-dev.pas-min.io/orders customer="http://e-shop-api-dev.pas-min.io/customers/1" qty=5

{
    "_links": {
        "customer": {
            "href": "http://172.31.7.160:27223/customers/1"
        }, 
        "item": {
            "href": "http://e-shop-api-dev.pas-mini.io/orders/3/item"
        }, 
        "order": {
            "href": "http://e-shop-api-dev.pas-mini.io/orders/3"
        }, 
        "self": {
            "href": "http://e-shop-api-dev.pas-mini.io/orders/3"
        }
    }, 
    "customer": {
        "firstName": null, 
        "lastName": null
    }, 
    "customerId": 1, 
    "qty": 5
}

```
두개의 객체가 릴리이션으로 잘 연결 되었다. 그런데 연결된 주소를 보면 내부 주소로 연결되었기 때문에 (마이크로서비스간의 통신에서는 문제가 되지 않으나, 외부 클라이언트, 특히 프론트엔드에서 연결될 때는 접근이 불가하다) 이를 라우터 주소인 "ttp://e-shop-api-dev.pas-mini.io/customers/1" 로 연결되면 좋겠다.

이 작업은 생각보다 간단하다. @RestAssociation 옵션 값 중 serviceId 를 "self"로 주면 호출된 URI 에 연결하여 다른 마이크로 서비스 일지라도 같은 host 주소로 link 를 만든다:

```java
    @RestAssociation(serviceId = "self", path="/customers/{customerId}", joinColumn = "customerId")
```

이렇게 해서 커밋한 후, 변경된 서비스로 호출하면 다음과 같이 동일한 호스트명을 유지한 채로 link들이 생성되고 외부접속한 클라이언트에서도 리소스들을 하나의 호스트내에서 탐색가능하다:

```

{
    "_links": {
        "customer": {
            "href": "http://e-shop-api-dev.pas-mini.io/customers/1"
        }, 
        "item": {
            "href": "http://e-shop-api-dev.pas-mini.io/orders/3/item"
        }, 
        "order": {
            "href": "http://e-shop-api-dev.pas-mini.io/orders/3"
        }, 
        "self": {
            "href": "http://e-shop-api-dev.pas-mini.io/orders/3"
        }
    }, 
    "customer": {
        "firstName": null, 
        "lastName": null
    }, 
    "customerId": 1, 
    "qty": 5
}
```
