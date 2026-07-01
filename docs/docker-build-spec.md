# Docker 빌드 및 실행 명세

## 목적

H-Links Docker 구성은 개발 편의와 앱 통합 테스트를 분리해서 사용합니다.

- 평소 개발 중에는 Oracle DB만 Docker로 실행합니다.
- Spring Boot 애플리케이션은 IntelliJ에서 실행합니다.
- 앱 컨테이너가 필요한 통합 테스트 상황에서만 별도 compose 파일을 함께 사용합니다.
- app 컨테이너에서는 Dockerfile로 설치된 ffmpeg를 사용합니다.
- IntelliJ 로컬 실행 시에는 각자 로컬 PC에 설치된 ffmpeg를 사용합니다.

## 파일 역할

| 파일 | 역할 |
| --- | --- |
| `docker-compose.yml` | Oracle DB 전용 compose 파일 |
| `docker-compose.app.yml` | Spring Boot app 컨테이너 실행용 compose 파일 |
| `Dockerfile` | Spring Boot app 이미지 생성 및 ffmpeg 설치 |
| `.gitignore` | 로컬 저장소 `/storage/` 및 테스트 파일 제외 |

## 개발 기본 실행

개발할 때는 Oracle DB만 Docker로 실행합니다.

```bash
docker compose up -d
```

컨테이너 상태 확인:

```bash
docker compose ps
```

동작 방식:

- `oracle` 컨테이너만 실행됩니다.
- Spring Boot는 IntelliJ에서 실행합니다.
- 애플리케이션 접속 주소는 `http://localhost:8080`입니다.
- 로컬 Spring Boot datasource URL은 `localhost:1522` 기준을 사용합니다.
- 로컬 ffmpeg는 각자 PC에 설치된 ffmpeg를 사용합니다.

## 앱 Docker 통합 테스트 실행

앱까지 Docker로 실행해야 할 때만 아래 명령을 사용합니다.

```bash
./gradlew clean bootJar
docker compose -f docker-compose.yml -f docker-compose.app.yml up --build
```

동작 방식:

- `oracle`과 `app` 컨테이너가 함께 실행됩니다.
- Docker app 접속 주소는 `http://localhost:8081`입니다.
- app 컨테이너 내부에서는 `oracle:1521`로 DB에 접근합니다.
- app 컨테이너 내부 ffmpeg는 Dockerfile에서 설치된 `ffmpeg`를 사용합니다.
- Dockerfile은 이미 빌드된 `build/libs/*.jar`를 복사하므로, app 이미지 빌드 전 `./gradlew clean bootJar`를 먼저 실행해야 합니다.

## 종료 명령

컨테이너만 중지:

```bash
docker compose -f docker-compose.yml -f docker-compose.app.yml down
```

Oracle DB만 실행한 경우:

```bash
docker compose down
```

Oracle 데이터 볼륨까지 삭제해야 하는 경우에만 아래 명령을 사용합니다.

```bash
docker compose down -v
```

주의: `-v` 옵션은 Oracle 데이터 볼륨을 삭제합니다.

## 환경 변수

프로젝트 루트의 `.env` 파일을 사용합니다.

```bash
ORACLE_PASSWORD= 
DB_USERNAME= 
DB_PASSWORD= 
```

`.env` 파일은 민감정보를 포함할 수 있으므로 Git에 올리지 않습니다.

## 팀 공지 요약

기본 개발은 DB만 Docker로 실행합니다.

```bash
docker compose up -d
```

앱 컨테이너까지 테스트할 때만 jar를 만든 뒤 app compose를 함께 실행합니다.

```bash
./gradlew clean bootJar
docker compose -f docker-compose.yml -f docker-compose.app.yml up --build
```

Docker app은 `http://localhost:8081`, IntelliJ 로컬 app은 `http://localhost:8080`을 사용합니다.
