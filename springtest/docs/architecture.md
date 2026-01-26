# Vulsite 아키텍처 다이어그램

## 1. 전체 시스템 아키텍처

```mermaid
flowchart TB
    subgraph Client["👤 클라이언트"]
        Browser["웹 브라우저"]
    end

    subgraph Server["🖥️ Spring Boot Server :8080"]
        subgraph Controllers["Controllers"]
            UC["UserController<br/>- 로그인/회원가입<br/>- 프로필 수정"]
            BC["BoardController<br/>- 게시글 CRUD<br/>- 검색"]
            FC["FileController<br/>- 파일 업로드<br/>- 파일 다운로드"]
            AC["AdminController<br/>- 대시보드<br/>- 사용자 관리"]
        end

        subgraph Services["Services"]
            US["UserService"]
            BS["BoardService"]
            FS["FileService"]
        end

        subgraph Repositories["Repositories (JPA)"]
            UR["UserRepository"]
            BR["BoardRepository"]
            FR["FileRepository"]
        end
    end

    subgraph Database["💾 H2 Database (In-Memory)"]
        UT[("users")]
        BT[("boards")]
        FT[("files")]
    end

    subgraph FileSystem["📁 File System"]
        UL["./uploads"]
    end

    Browser -->|HTTP| UC & BC & FC & AC
    UC --> US --> UR --> UT
    BC --> BS --> BR --> BT
    FC --> FS --> FR --> FT
    FS --> UL
```

## 2. 취약점 매핑 아키텍처

```mermaid
flowchart LR
    subgraph Vulnerabilities["🔓 10가지 취약점"]
        V1["#1 SQL Injection"]
        V2["#2 Stored XSS"]
        V3["#3 File Upload"]
        V4["#4 Path Traversal"]
        V5["#5 버전 정보 노출"]
        V6["#6 H2 Console 노출"]
        V7["#7 평문 계정 정보"]
        V8["#8 관리자 페이지 접근"]
        V9["#9 IDOR 사용자 수정"]
        V10["#10 IDOR 게시글 삭제"]
    end

    subgraph Location["📍 취약점 위치"]
        UC2["UserController.java"]
        US2["UserService.java"]
        BC2["BoardController.java"]
        BS2["BoardService.java"]
        FC2["FileController.java"]
        FS2["FileService.java"]
        AC2["AdminController.java"]
        AP["application.properties"]
    end

    V1 --> US2
    V2 --> BS2
    V3 --> FS2
    V4 --> FS2
    V5 --> AP
    V6 --> AP
    V7 --> AP
    V8 --> AC2
    V9 --> UC2
    V10 --> BC2
```

## 3. 데이터 흐름 (SQL Injection 예시)

```mermaid
sequenceDiagram
    participant A as 공격자
    participant B as Browser
    participant C as UserController
    participant S as UserService
    participant E as EntityManager
    participant D as H2 Database

    A->>B: username: ' OR '1'='1' --
    B->>C: POST /user/login
    C->>S: loginVulnerable(username, password)
    S->>E: createNativeQuery(취약한 SQL)
    Note over E: SELECT * FROM users<br/>WHERE username = '' OR '1'='1' --'<br/>AND password = '...'
    E->>D: 실행
    D-->>E: 모든 사용자 반환
    E-->>S: User 객체
    S-->>C: User (첫 번째)
    C-->>B: 로그인 성공 (redirect)
    B-->>A: 관리자로 로그인됨!
```

## 4. 테스트 아키텍처

```mermaid
flowchart TB
    subgraph TestTypes["🧪 테스트 유형"]
        subgraph MockMvc["MockMvc 테스트"]
            T1["SqlInjectionTest"]
            T9["IdorUserUpdateTest"]
            T10["IdorBoardDeleteTest"]
        end

        subgraph Playwright["Playwright 테스트"]
            T2["StoredXssTest"]
        end
    end

    subgraph Target["🎯 테스트 대상"]
        API["Spring Controllers"]
        UI["웹 브라우저 UI"]
    end

    MockMvc -->|직접 호출| API
    Playwright -->|HTTP 요청| API
    Playwright -->|렌더링| UI
```

---

## 이미지 생성 방법

### 방법 1: Mermaid Live Editor (권장)
1. https://mermaid.live 접속
2. 위 코드 복사 & 붙여넣기
3. PNG/SVG 다운로드

### 방법 2: VS Code 확장
1. "Markdown Preview Mermaid Support" 설치
2. 이 파일 열기 → 미리보기

### 방법 3: GitHub
- GitHub에서 이 파일을 보면 자동 렌더링됨

### 방법 4: 명령줄
```bash
npm install -g @mermaid-js/mermaid-cli
mmdc -i architecture.md -o architecture.png
```
