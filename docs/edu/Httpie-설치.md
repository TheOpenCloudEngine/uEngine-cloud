Httpie를 설치를 위해서는 Python이 필요하다. [Python 다운로드 페이지](https://www.python.org/downloads/) 

```설치순서
Python 다운로드 후 설치한다.
설치 과정중 pip를 꼭 체크하여서 설치해야한다.
```
![python-install](https://user-images.githubusercontent.com/16382067/35021672-3574cbb2-fb75-11e7-9bfe-7fce60fb03d6.png)
![python-install2](https://user-images.githubusercontent.com/16382067/35021680-3ffcca9e-fb75-11e7-86b1-822446a08d19.png)
![python-install3](https://user-images.githubusercontent.com/16382067/35021682-415d8e8c-fb75-11e7-8ecf-8261c47453ec.png)
![python-install4](https://user-images.githubusercontent.com/16382067/35021685-437aabb4-fb75-11e7-9e49-db541ab9eeb1.png)

```
설치가 끝나면 command창을 실행 후, py를 입력하면 아래와 같은 내용의 메세지가 뜨면 Python의 설치가 완료 된 것이다.
```
![python-install5](https://user-images.githubusercontent.com/16382067/35022245-3f7bd5b2-fb78-11e7-8241-4f03f45d4479.png)

```환경변수 설정
pip를 위한 환경변수 설정이 필요하다. 환경변수는 내컴퓨터 -> 고급 시스템 설정 -> 고급탭 -> 환경변수 -> 시스템 변수 -> path에

{Python설치 경로}\Scripts 을 입력하여 추가해준다.

ex) C:\Python\Python36-32\Scripts
```
파이썬의  환경변수 등록까지 모두 완료됐다면 pip를 통해 httpie를 설치해준다. 커맨드창에서 아래의 명령어를 입력해 주면 된다.
```
pip install -U httpie
```
![pip-install](https://user-images.githubusercontent.com/16382067/35022908-a79dda7a-fb7b-11e7-9580-8ea94a3ddfac.png)
위의 그림과 같이 httpie가 모두 설치가 끝난다면 커맨드창에 아래의 명령어를 입력해본다.
```
http http://www.example.com
```
![httpie](https://user-images.githubusercontent.com/16382067/35022941-c9ffb11a-fb7b-11e7-90c5-2bd48987302d.png)
위의 이미지와 같은 결과의 화면이 나온다면 Httpie의 설치가 끝난것이다.
