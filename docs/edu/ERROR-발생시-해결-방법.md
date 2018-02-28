# Metaworks4
1. ADD /target/*.jar app.jar에서 발생하는 에러
![image](https://user-images.githubusercontent.com/16382067/35031382-0d86e384-fba6-11e7-8a97-12ef22fbaf45.png)

이 경우, 해당 어플리케이션의 gitlab repository에서 Dockerfile을 수정해주면됨.

```
ADD /target/*.jar app.jar -> ADD /target/*-exec.jar app.jar
```

# Vuejs
1. Docker 업로드 실패
![image](https://user-images.githubusercontent.com/16382067/35031459-65e8a63e-fba6-11e7-9274-7cc7d91383e7.png)

    1. 처음부터 빌드를 시작(파일을 수정한 후 커밋하면 재 빌드 시작함)
    1. 앱을 지우고 다시 만들면 됨.
