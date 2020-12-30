# Simplified KeyLogger
손쉽게 사용 가능하고 읽기 편한 인터페이스를 제공하는 실시간 입력 장치 정보 제공 어플리케이션.
<br><br>
### 요구사항
사용을 위해서는 다음의 환경이 필요합니다.
- [JDK 8](https://java.com/en/download)
- [Git](https://git-scm.com)
<br><br>
### 키로거 빌드하기
터미널을 열고 다음의 명령을 우선 입력합니다.
```bash
$ cd ~
$ git clone https://github.com/OrigamiDream/juikit.git
$ cd juikit
$ ./mvnw clean install
$ cd ..
$ rm -rf juikit
```
위 명령어로 필요한 라이브러리를 컴퓨터에 설치하게 됩니다.<br>
이어서, 키로거 어플리케이션을 빌드하려면 다음의 명령들을 입력합니다.
```bash
$ git clone https://github.com/OrigamiDream/keylogger.git
$ cd keylogger
$ ./mvnw clean package -U
$ cd target
```
컴파일이 완료된 실행 파일이 `target` 디렉토리 안에 생성됩니다.
<br><br>
### 키로거 사용하기
새로운 터미널을 열고 다음의 명령어를 입력하여 키로거를 실행할 수 있습니다.
```bash
$ cd keylogger/target
$ java -jar keylogger-1.0-SNAPSHOT.jar
```

### 주의사항

- 키가 입력되는 모습과 마우스 동작을 보려면 반드시 녹화 버튼을 사용해야 합니다.
- macOS 에서는 시스템 환경설정에서 `손쉬운 사용` 권한을 허용하고 어플리케이션을 재시작해 주어야 합니다.


### TODO

- 장시간 녹화를 이용할 경우 저장과정에서 StackOverflowError 를 만나게 됩니다. 수정 예정은 없습니다.