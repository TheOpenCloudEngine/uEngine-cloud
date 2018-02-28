# OCE 플랫폼 계정 등록 및 접속
1. [http://cloud.pas-mini.io](http://cloud.pas-mini.io/) 에 접속하면 아래와 같은 화면에서 SIGN UP 버튼을 누른다.
![Login](https://user-images.githubusercontent.com/16382067/34975851-21bbbe26-fad7-11e7-80e4-580c726d9e51.png)

1. SIGN UP 화면에서 E-mail과, 패스워드, 이름을 입력 후 SIGN UP 버튼을 누르면 메일로 승인 확인 메일이 온다.
![Signup](https://user-images.githubusercontent.com/16382067/34975938-7aa64704-fad7-11e7-8a33-954bc72bbf4a.png)

1. 승인 확인 메일에서 가입 확인 글씨를 눌러주면 가입이 완료 된다.
![Mail](https://user-images.githubusercontent.com/16382067/34976111-54163ff8-fad8-11e7-8d6b-079b678a6719.png)

# 애플리케이션 생성
1. 로그인을 하면 아래와 같은 화면으로 넘어간다.
![Main](https://user-images.githubusercontent.com/16382067/34978941-3bdb8c4e-fae3-11e7-8c29-117a9d77dc20.png)

1. 위 그림의 작성버튼을 클릭하면 아래와 같은 화면으로 넘어간다.
![App](https://user-images.githubusercontent.com/16382067/34979768-ace15854-fae5-11e7-9d59-4be0fc2e061d.png)

1. 해당 예제에서는 Metaworks4 (Netfflix OSS)를 사용하므로 Metaworks4를 선택하면 아래와 같은 화면으로 넘어간다.
![image](https://user-images.githubusercontent.com/16382067/34980801-f44587e4-fae8-11e7-86c6-8727351f719b.png)
    1) 해당 어플리케이션의 이름을 작성해준다. 
    2) 외부접속을 위한 도메인을 작성하여준다. 
     (프로덕션주소를 작성하면, 스테이징과 개발주소는 자동으로 입력되어진다.)
    ![image](https://user-images.githubusercontent.com/16382067/34980956-670cc33c-fae9-11e7-9798-b56bf4257ee9.png)

1. 화면생성이 되면 아래와 같은 화면으로 넘어간다.
![image](https://user-images.githubusercontent.com/16382067/34981296-6f23eedc-faea-11e7-8c31-ae8ea9290a51.png)

1. 왼쪽 메뉴의 빌드 및 배포를 접속하면 아래와 같은 화면이 나온다.
![image](https://user-images.githubusercontent.com/16382067/34981374-ac8662a0-faea-11e7-96b4-54d0f4c76955.png)

1. 현재 빌드 및 배포중인 어플리케이션의 상태를 보여주며, 상태의 Running을 누르면 Gitlab으로 넘어가져서 좀 더 자세한 진행상황을 볼 수 있다.
![image](https://user-images.githubusercontent.com/16382067/34981441-d8b98bd6-faea-11e7-991f-135a8449df84.png)

# 코드 다운로드 및 빌드
1. 이전의 5번이미지에서 소스코드를 선택하면 해당 어플리케이션의 소스코드를 확인 할 수 있다.
![image](https://user-images.githubusercontent.com/16382067/34981934-63195210-faec-11e7-8321-067bb80e22cc.png)

1. 위의 스크린샷에서 빨간색 부분을 클릭하면 git의 주소가 나오고 git으로 내려받을 수 있다.
```
ex) git clone http://gitlab.pas-mini.io/sanghoon/testapp.git
```

1. 빌드는 소스코드에 오류가 없다면 커밋시, 자동으로 반영이 된다.

# IDE 접속
1. IDE를 실행 후, Import Project -> git 다운받은 경로에서 pom.xml을 선택하여 IDE로 Import 할 수 있다.

# 서비스 접속
1. 서비스 접속방법은 웹브라우저에
```
http://testapp-dev.pas-mini.io/
```
를 입력하여 접속하면,
```
{
  "_links" : {
    "customers" : {
      "href" : "http://testapp-dev.pas-mini.io/customers{?page,size,sort}",
      "templated" : true
    },
    "profile" : {
      "href" : "http://testapp-dev.pas-mini.io/profile"
    }
  }
}
```
이러한 페이지가 나오면 정상적으로 빌드가 되어 실행 된 것이다.

또는 Httpie를 이용하여 확인 할 수 있다.

Httpie가 설치되어있지 않다면 [Httpie 설치 가이드](edu/Httpie-설치.md)를 확인하여 설치한다.

httpie를 이용한 확인방법은 아래와 같다.

```
커맨드창에서

httpie testapp-dev.pas-mini.io를 입력하면 된다.
```
![image](https://user-images.githubusercontent.com/16382067/35023129-a9d1288c-fb7c-11e7-9030-f9ec22a04592.png)

위와 같은 메세지가 뜬다면 정상적으로 서비스가 실행 된 것이다.

# 테스트

해당 문서에서는 테스트를 위하여 Httpie를 사용한다.
Httpie가 설치되어있지 않다면 [Httpie 설치 가이드](edu/Httpie-설치.md)를 확인하여 설치한다.

1. 현재 실행중인 서비스를 확인
![image](https://user-images.githubusercontent.com/16382067/35023129-a9d1288c-fb7c-11e7-9030-f9ec22a04592.png)

현재 실행중인 서비스에는 customers라는 게 있다는 것을 확인할 수 있다.

우선 customers를 확인해보려면

```
http testapp-dev.pas-mini.io/customers
```
위의 명령어를 실행하면된다. 명령어를 실행하고 나면 아래 화면이 출력된다.
![image](https://user-images.githubusercontent.com/16382067/35025189-4a35c8c8-fb87-11e7-9114-a28376e29536.png)
"_embedded"를 확인해 보면 현재 customers에는 아무것도 존재하지 않는다는 것을 확인 할 수 있다.

2. 데이터 추가를 위해 customers에는 어떠한 속성값이 필요한지 확인
```
http testapp-dev.pas-mini.io/profile/customers
```
위 명령어를 입력하면 아래 화면이 출력된다.
![image](https://user-images.githubusercontent.com/16382067/35025239-b24c3e24-fb87-11e7-9de4-acc77ca1df95.png)
현재 customers에 필요한 속성값은 
```
"descriptors": [
                    {
                        "name": "firstName",
                        "type": "SEMANTIC"
                    },
                    {
                        "name": "lastName",
                        "type": "SEMANTIC"
                    }
                ],
```
을 보면 firstName과 lastName 2가지가 필요하다는 것을 확인 할 수 있다.

3. 데이터 추가
```
http testapp-dev.pas-mini.io/customers firstName="Kim" lastName="sanghoon"
(http testapp-dev.pas-mini.io/{{추가할 주소}} {{인자 값}}
```
의 형태로 명령어를 보내면 아래와 같은 화면이 나온다.
![image](https://user-images.githubusercontent.com/16382067/35025455-f9101c62-fb88-11e7-8a38-caedd4dbd88d.png)

```
{
    "_links": {
        "customer": {
            "href": "http://testapp-dev.pas-mini.io/customers/3"
        },
        "self": {
            "href": "http://testapp-dev.pas-mini.io/customers/3"
        }
    },
    "firstName": "kim",
    "lastName": "sanghoon"
}
```
해당 결과를 보면 fisrtName에는 Kim, lastName에는 sanghoon이 들어간것을 확인 할 수 있다.

다시 이전의 customers로 들어가보면
```
http testapp-dev.pas-mini.io/customers
```
![image](https://user-images.githubusercontent.com/16382067/35025758-a853ca38-fb8a-11e7-90ee-918b8e9b347f.png)

이전과 다르게, "_embedded"에 데이터가 추가된 것을 확인 할 수 있다.

# 프로덕션

위의 내용은 모두 dev서버를 이용한 방법이므로 testapp-dev.pas-mini.io와 같이 -dev가 붙어있다.

이것을 프로덕션 하기위해서 우선 생성한 앱의 대시보드를 들어가면 아래와 같은 화면으로 넘어간다.
![image](https://user-images.githubusercontent.com/16382067/35026073-36bc222e-fb8c-11e7-913e-5bc3f83ae38f.png)

이곳에서 라우트 버튼을 클릭하면 생성시에 입력했던 주소값들을 모두 확인 할 수 있다.
![image](https://user-images.githubusercontent.com/16382067/35026115-691bca8a-fb8c-11e7-9737-9df72d1316fc.png)

이곳에서 프로덕션 외부 주소로 httpie를 보내보면
```
http testapp.pas-mini.io
```
![image](https://user-images.githubusercontent.com/16382067/35026150-9a08f708-fb8c-11e7-8261-3058a570befe.png)

현재는 프로덕션이 실행되어있지 않기때문에 에러가 발생한다.

라우트 목록을 종료 후, 대쉬보드에서 프로덕션 체크 박스를 체크해주면 아래와 같이 변하는데,
![image](https://user-images.githubusercontent.com/16382067/35026555-202f68b0-fb8f-11e7-8982-15bf27f5078e.png)
![image](https://user-images.githubusercontent.com/16382067/35026579-4df0bb28-fb8f-11e7-9dff-c8f155bc69ce.png)

이곳에서 '태그 또는 브랜치에서'의 링크를 클릭하면 아래와 같은 화면이 나온다.

![image](https://user-images.githubusercontent.com/16382067/35026634-8dd5daca-fb8f-11e7-9a8d-3a6de1a36cfa.png)
이곳에서는 배포할 브런치 또는 태그를 선택해 주어야한다. 예제에서는 따로 커밋한게 없으므로, master를 선택하였다.

선택 후 Continue를 진행하면 아래와 같은 화면까지 진행이 된다.
![image](https://user-images.githubusercontent.com/16382067/35026671-c4110484-fb8f-11e7-919b-05c94f4e0b37.png)

예제에서는 환경을 맞춰주기 위해서 stg 런타임 환경을 그대로 사용하기를 선택하였다. 
그후 finish를 하면 잠시뒤 창이 닫히면서 프로덕션의 빌드 및 배포가 시작된다.

왼쪽 메뉴의 빌드 및 배포를 클릭해보면

![image](https://user-images.githubusercontent.com/16382067/35026734-1d9312a4-fb90-11e7-85b8-fd001c228679.png)

위와 같이 dev이외의 한개의 서비스가 생성되는것을 확인 할 수 있다.

이후 내용은 상단의 [어플리케이션을 첫 생성](edu/OCE-MSA-플랫폼의-사용.md)할때와 같다.

production의 배포가 완료가 됐다면 다시 production 주소로 httpie를 이용하여 보내면
```
http testapp.pas-mini.io
```
![image](https://user-images.githubusercontent.com/16382067/35027729-a0f93c36-fb95-11e7-8f1b-a9087b6750ad.png)

배포가 완료되어 정상적으로 작동되는 것을 확인 할 수 있다.