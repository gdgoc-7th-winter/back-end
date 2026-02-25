## 📏 코드 컨벤션

아래 명령어를 이용하여 convention을 지켜주세요

```bash
git config core.hooksPath .github/hooks
```

아래 명령어를 이용해서 코드 스타일을 준수했는지 확인해주세요

```bash
./gradlew checkstyleMain checkstyleTest
```

## 🔧 로컬 개발

아래 명령어를 통해 쉽게 도커 컨테이너에서 사용할 수 있습니다

```bash
docker compose -f docker-compose-local.yml up -d   # 인프라 (PostgreSQL + Redis)
```

PR을 올리기 전에 빌드 테스트를 진행해주세요

```bash
./gradlew clean build
```