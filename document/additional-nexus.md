Nexus 는 maven 에서 사용할 수 있는 가장 널리 사용되는 무료 repository 중의 하나로, maven,npm,yum 등 다양한 repository 를 사용할 수 있습니다.

Local 에 nexus 를 설치하게 되면, 외부로 부터 dependency 를 끌어 오는 수고를 덜고, local nexus 를 proxy (cache)로 사용함으로써 빠르게 라이브러리들을 끌어 올 수 도 있고, 
반대로 개발팀내에서 사용하는 공통 라이브러리들을 local nexus 에 배포해서 팀간에 공유할 수 도 있습니다.

또한 사용자 계정 지정을 통해서 repository 에 대한 접근 정책을 정의할 수 도 있습니다.

Nexus 는 repository 의 용도와 목적에 따라서 몇 가지로 나눌 수 있는데, 대표적으로 다음과 같은 종류 들이 있다.

①   Snapshots : 빌드등 수시로 릴리즈 되는 바이너리를 배포 하는 장소

②   Releases : 정식 릴리즈를 통해서 배포되는 바이너리를 저장하는 저장소

③   3rd party : 벤더등에서 배포하는 (Oracle,IBM등) 바이너리를 저장해놓는 장소로 특정 솔루션등을 사용할때, 딸려 오는 라이브러리등을 여기에 놓고 사용한다

④   Proxy Repository : 원격에 원본 repository가 있는 경우, Local에 캐쉬 용도로 사용한다.

⑤   Virtual Repository : Repository Group은 몇 개의 repository를 하나의 repository로 묶어서 단일 접근 URL을 제공한다.

 

여기서는 가장 널리 사용하는 local repository로 설정 하는 시나리오와 함께, 외부 repository에 대한 proxy 시나리오로 사용하는 설정을 소개한다.