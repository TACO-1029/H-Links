# H-Links
---

## 👥 팀 구성원

<table width="50%" align="center">
    <tr>
        <td align="center"><b>팀장</b></td>
        <td align="center"><b>팀원</b></td>
        <td align="center"><b>팀원</b></td>
        <td align="center"><b>팀원</b></td>
    </tr>
    <tr>
        <td align="center"><img src="https://avatars.githubusercontent.com/u/147317355?v=4"></td>
        <td align="center"><img src="https://avatars.githubusercontent.com/u/80496853?v=4"></td>
        <td align="center"><img src="https://avatars.githubusercontent.com/u/202517610?v=4"></td>
        <td align="center"><img src="https://avatars.githubusercontent.com/u/181914316?v=4"></td>
    </tr>
    <tr>
        <td align="center"><b><a href="https://github.com/woodgeon">도건우</a></b></td>
        <td align="center"><b><a href="https://github.com/jaewonwi">위재원</a></b></td>
        <td align="center"><b><a href="https://github.com/CheolyongKim">김철용</a></b></td>
        <td align="center"><b><a href="https://github.com/woojin-devv">최우진</a></b></td>
    </tr>
</table>

## 🐳 Docker 실행 가이드

프로젝트 루트의 `docker-compose.yml` 기준으로 실행합니다.

### 1. Docker Compose 서비스명

| 서비스명 | 컨테이너 이름 | 역할 |
| --- | --- | --- |
| `app` | `h-links-app` | Spring Boot 애플리케이션 |
| `oracle` | `h-links-oracle` | Oracle XE DB |
| `chroma` | `h-links-chroma` | Chroma Vector DB |

`container_name`은 실제 컨테이너 이름이고, `docker compose` 명령어에서는 `services` 아래의 서비스명인 `app`, `oracle`, `chroma`를 사용합니다.

### 2. 실행 명령어

전체 빌드 및 실행:

```bash
docker compose up -d --build
```

`app` 서비스만 다시 빌드 및 실행:

```bash
docker compose up -d --build app
```

실행 상태 확인:

```bash
docker compose ps
```

`app` 로그 확인:

```bash
docker compose logs -f app
```

접속 주소:

```bash
http://localhost:8081
```

### 3. 환경변수

프로젝트 루트의 `.env` 파일에 필요한 값을 설정합니다. `.env` 파일은 민감정보를 포함할 수 있으므로 Git에 올리지 않습니다.

```bash
ORACLE_PASSWORD=oracle 관리자 계정 비밀번호
DB_USERNAME=Spring Boot가 접속할 앱 전용 DB 계정
DB_PASSWORD=Spring Boot가 접속할 앱 전용 DB 비밀번호
OPENAI_API_KEY=OpenAI API Key
AWS_ACCESS_KEY_ID=AWS Access Key
AWS_SECRET_ACCESS_KEY=AWS Secret Key
```

- `ORACLE_PASSWORD`는 Spring Boot 앱이 직접 사용하는 비밀번호가 아닙니다. Oracle Docker 이미지가 `SYS`, `SYSTEM` 같은 관리자 계정 비밀번호를 초기화할 때 사용하는 값입니다.
- Spring Boot 앱은 `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`에 매핑되는 `DB_USERNAME`, `DB_PASSWORD`를 사용합니다.

### 4. 주의사항

아래 명령어는 잘못된 명령어입니다.

```bash
docker compose up -d --build h-links-app
```

`h-links-app`은 컨테이너 이름이고, compose 서비스명은 `app`입니다. `app`만 다시 빌드 및 실행할 때는 아래 명령어를 사용합니다.

```bash
docker compose up -d --build app
```

`docker compose down -v`는 `oracle-data`, `chroma-data` 볼륨까지 삭제합니다. DB 데이터와 Chroma 데이터가 날아갈 수 있으므로 필요한 경우에만 사용합니다.

일반적인 재빌드 상황에서는 아래 명령어 중 하나를 사용합니다.

```bash
docker compose up -d --build app
docker compose up -d --build
```

## 🦴 Commit Convention
### 커밋 메시지 규칙
커밋 메시지는 아래 형식으로 작성합니다:
```
<타입>:<내용>

ex) feat: 로그인 기능 추가
```
<br>

| 타입         | 설명                     |
| ---------- | ---------------------- |
| `feat`     | 새로운 기능 추가              |
| `fix`      | 버그 수정                  |
| `docs`     | 문서 수정 (README 등)       |
| `style`    | 코드 포맷팅, 세미콜론 누락 등      |
| `refactor` | 코드 리팩토링 (기능 변화 없음)     |
| `test`     | 테스트 코드 추가 또는 수정        |
| `chore`    | 기타 변경사항 (빌드 설정, 패키지 등) |

## 🌙 Git Flow 브랜치 전략
### 주요 브랜치
| 브랜치 이름      | 용도                                     |
| ----------- | -------------------------------------- |
| `main`      | 배포(Release)가 이루어지는 안정적인 코드             |
| `dev`   | 다음 릴리스를 준비하는 개발 브랜치                    |

---
### 브랜치 네이밍
브랜치 네이밍은 아래 형식으로 작성합니다:
```
<타입>/<이슈번호>

ex)feat/#23
```
| 타입         | 설명                |
| ---------- | ----------------- |
| `feat`  | 새로운 기능 작업         |
| `fix`      | 버그 수정 작업          |
| `hotfix`   | 급한 수정 작업 (배포 후 등) |
| `refactor` | 코드 리팩토링           |
| `docs`     | 문서 작업             |
| `chore`    | 기타 작업 (설정, 패키지 등) |

### ☸️ Git 브랜치 및 개발 프로세스

1. **기능 개발 시작**
    - `dev` 브랜치에서 새로운 `feat` 브랜치를 생성하여 개발을 시작합니다.

2. **기능 개발 및 커밋**
    - `feat` 브랜치에서 기능을 완성하고 커밋을 진행합니다.

3. **코드 리뷰 및 병합**
    - 개발 완료 후, `feat` 브랜치에서 `dev` 브랜치로 PR(Pull Request)을 생성하여 코드 리뷰를 받습니다.
    - 리뷰가 완료되면 `dev` 브랜치에 병합합니다.

4. **테스트**
    - `dev` 브랜치에서 배포 전 최종 기능들이 안정적으로 동작하는지 테스트합니다.

5. **배포**
    - 테스트가 완료되면 `dev` 브랜치를 `main` 브랜치에 병합하여 최종 배포를 진행합니다.
