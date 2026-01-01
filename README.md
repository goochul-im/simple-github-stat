# GitHub Stats Generator

GitHub 사용자 활동 통계와 언어 사용량 비율을 분석하여 SVG 카드로 생성해주는 Spring Boot 애플리케이션입니다.
기존에 사용하던 github-readme-stat 이 제대로 동작하지 않고 터지는 일이 자주 발생하여 직접 제작하였습니다.

![GitHub Stats Example](src/main/resources/templates/stats.svg)
*(위 이미지는 템플릿 예시이며, 실제 실행 시 데이터가 동적으로 채워집니다.)*

## 🛠️ 기술 스택 (Tech Stack)

*   **Language**: Kotlin
*   **Framework**: Spring Boot 3.4.1
*   **Build Tool**: Gradle (Kotlin DSL)
*   **API**: GitHub GraphQL API (v4) & REST API (v3)
*   **Template Engine**: Thymeleaf (SVG Rendering)
*   **Test**: JUnit 5, Mockito

## ✨ 주요 기능 (Features)

1.  **종합 활동 통계**:
    *   **Total Stars**: 내가 받은 스타 수 합계
    *   **Total Commits**: 전체 커밋 수
    *   **Last Month**: 최근 30일간의 커밋 활동 (New!)
    *   **PRs & Issues**: 전체 Pull Requests 및 Issues 개수
2.  **언어 분석**: 리포지토리 개수가 아닌, 실제 작성된 **코드의 바이트(Byte)** 크기를 기준으로 언어 사용 비율을 분석합니다.
    *   GitHub GraphQL API를 사용하여 빠르고 정확합니다.
    *   GitHub 공식 색상 코드를 자동으로 적용합니다.
3.  **디자인**: Midnight Blue 테마의 다크 모드 디자인과 도넛 차트를 제공합니다.
4.  **옵션**:
    *   특정 리포지토리 제외 (`exclude`)
    *   특정 언어 숨기기 (`hide`)
    *   조직(Organization) 리포지토리 포함 여부 선택 (`include_orgs`)

## 🚀 시작하기 (Getting Started)

### 필수 요구 사항 (Prerequisites)
*   Java 17 이상
*   GitHub Personal Access Token (Classic)
    *   권한: 공개 리포지토리만 분석할 경우 **권한(Scope) 체크 불필요** (No scope).
    *   비공개(Private) 리포지토리까지 포함하려면 `repo` 권한이 필요합니다.

### 설치 및 실행 (Installation & Run)

1.  **프로젝트 클론**
    ```bash
    git clone https://github.com/your-username/github-stats.git
    cd github-stats
    ```

2.  **환경 변수 설정 (GitHub Token)**
    GitHub API 호출 제한(Rate Limit)을 피하기 위해 토큰 설정이 필수입니다.
    
    **Windows (CMD)**:
    ```cmd
    set GITHUB_TOKEN=your_token_here
    ```
    
    **Linux / macOS**:
    ```bash
    export GITHUB_TOKEN=your_token_here
    ```

3.  **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```

## 📖 API 사용법 (Usage)

서버가 실행되면(`http://localhost:8080`), 다음과 같이 API를 호출하여 SVG 이미지를 얻을 수 있습니다.

### 기본 사용법
```http
GET /api/stats?username={github_username}
```

### 파라미터 설명

| 파라미터 | 타입 | 필수 여부 | 기본값 | 설명 |
| :--- | :--- | :--- | :--- | :--- |
| `username` | String | **Yes** | - | 조회할 GitHub 사용자 ID |
| `exclude` | String | No | - | 통계에서 제외할 리포지토리 이름 (쉼표로 구분) <br> 예: `exclude=repo1,repo2` |
| `hide` | String | No | - | 통계 그래프에서 제외할 언어 이름 (쉼표로 구분, 대소문자 무관) <br> 예: `hide=html,css` |
| `include_orgs` | Boolean | No | `false` | 사용자가 속한 조직(Organization)의 리포지토리 포함 여부 <br> `true`: 조직 포함 / `false`: 개인 소유만 |

### 사용 예시

**1. 기본 조회**
```
http://localhost:8080/api/stats?username=gooch
```

**2. 특정 언어 제외**
주로 사용하는 언어(Java, Kotlin 등)만 강조하고 싶을 때 유용합니다.
```
http://localhost:8080/api/stats?username=gooch&hide=html,css
```

**3. 특정 리포지토리 제외**
'test-repo'와 'demo'를 제외하고 계산합니다.
```
http://localhost:8080/api/stats?username=gooch&exclude=test-repo,demo
```

**4. 조직 리포지토리 포함**
내가 속한 조직의 프로젝트까지 모두 합산합니다.
```
http://localhost:8080/api/stats?username=gooch&include_orgs=true
```

## 📌 GitHub Profile에 적용하기

GitHub 프로필의 `README.md`에 다음과 같이 이미지 태그를 추가하세요. (서버가 배포되어 있어야 합니다.)

```markdown
![My Stats](http://your-server-domain.com/api/stats?username=gooch&hide=html,css)
```
