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