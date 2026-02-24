## 📏 코드 컨벤션

아래 명령어를 이용하여 convention을 지켜주세요

```bash
git config core.hooksPath .github/hooks
```

아래 명령어를 이용해서 코드 스타일을 준수했는지 확인해주세요

```bash
./gradlew checkstyleMain checkstyleTest
```

## 🚀 실행

아래 명령어를 통해 쉽게 도커 컨테이너에서 사용할 수 있습니다

```bash
./gradlew clean build -x test
docker-compose up --build -d
```

## 🔧 로컬 개발

```bash
docker-compose up -d                    # 인프라 (PostgreSQL + Redis)
./gradlew bootRun                       # 앱 실행
./gradlew build                         # 빌드 + 테스트 + checkstyle + jacoco
```
