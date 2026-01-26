# 취약점 2차 수정 보고서

**작성일**: 2025-01-26
**수정 단계**: 2차 (불완전한 수정)
**목적**: 보안 학습을 위한 단계별 취약점 패치 및 우회 방법 학습

---

## 수정 요약

| 취약점 | 수정 파일 | 보안 대책 | 우회 가능 여부 |
|--------|----------|----------|---------------|
| XSS | BoardService.java | `<script>`, `<img>` 태그 필터링 | O |
| SQL Injection | UserService.java | 싱글쿼트 + 더블쿼트 제거 | O |
| File Upload | FileService.java | JSP 관련 확장자 블랙리스트 | O |
| Path Traversal | FileService.java | ../ 재귀적 제거 | O |
| IDOR | BoardController.java | 수정/삭제 작성자 확인 | O |
| Admin Page | AdminController.java | dashboard/users 관리자 확인 | O |

---

## 1. XSS (Cross-Site Scripting)

### 수정 파일
- `src/main/java/com/vulsite/service/BoardService.java`

### 수정 내용

**변경 전 (1차):**
```java
public Board create(String title, String content, User author) {
    // 1차 수정: <script> 태그만 필터링 (불완전)
    String filteredTitle = title.replaceAll("<script>", "").replaceAll("</script>", "");
    String filteredContent = content.replaceAll("<script>", "").replaceAll("</script>", "");

    Board board = new Board(filteredTitle, filteredContent, author);
    return boardRepository.save(board);
}
```

**변경 후 (2차):**
```java
public Board create(String title, String content, User author) {
    // 2차 수정: <script>, <img> 태그 필터링 (불완전)
    String filteredTitle = filterXss(title);
    String filteredContent = filterXss(content);

    Board board = new Board(filteredTitle, filteredContent, author);
    return boardRepository.save(board);
}

private String filterXss(String input) {
    if (input == null) return null;
    return input
            .replaceAll("<script>", "").replaceAll("</script>", "")
            .replaceAll("<img", "&lt;img");
}
```

### 적용된 보안 대책
- **필터링 방식**: 블랙리스트 기반 확장
- **필터링 대상**: `<script>`, `</script>`, `<img` 태그
- **필터링 방법**: 공통 메서드 `filterXss()` 추출

### 우회 방법
| 우회 페이로드 | 설명 |
|--------------|------|
| `<svg onload=alert(1)>` | SVG 태그 이벤트 핸들러 |
| `<iframe src="javascript:alert(1)">` | iframe javascript 프로토콜 |
| `<body onpageshow=alert(1)>` | body 태그 이벤트 핸들러 |
| `<marquee onstart=alert(1)>` | marquee 태그 이벤트 |

---

## 2. SQL Injection

### 수정 파일
- `src/main/java/com/vulsite/service/UserService.java`

### 수정 내용

**변경 전 (1차):**
```java
public User loginVulnerable(String username, String password) {
    // 1차 수정: 싱글쿼트만 제거 (불완전)
    String filteredUsername = username.replace("'", "");
    String filteredPassword = password.replace("'", "");

    String query = "SELECT * FROM users WHERE username = '" + filteredUsername + "' AND password = '" + filteredPassword + "'";
    List<User> users = entityManager.createNativeQuery(query, User.class).getResultList();
    return users.isEmpty() ? null : users.get(0);
}
```

**변경 후 (2차):**
```java
public User loginVulnerable(String username, String password) {
    // 2차 수정: 싱글쿼트, 더블쿼트 제거 (불완전)
    String filteredUsername = filterSqlChars(username);
    String filteredPassword = filterSqlChars(password);

    String query = "SELECT * FROM users WHERE username = '" + filteredUsername + "' AND password = '" + filteredPassword + "'";
    List<User> users = entityManager.createNativeQuery(query, User.class).getResultList();
    return users.isEmpty() ? null : users.get(0);
}

private String filterSqlChars(String input) {
    if (input == null) return null;
    return input.replace("'", "").replace("\"", "");
}
```

### 적용된 보안 대책
- **필터링 방식**: 블랙리스트 기반 확장
- **필터링 대상**: 싱글쿼트(`'`), 더블쿼트(`"`)
- **필터링 방법**: 공통 메서드 `filterSqlChars()` 추출

### 우회 방법
| 우회 페이로드 | 설명 |
|--------------|------|
| `admin\ OR 1=1--` | 백슬래시 이스케이프 |
| `admin OR 1=1--` | 쿼트 없이 조건 삽입 |
| `UNION SELECT * FROM users--` | SQL 키워드 사용 |

---

## 3. File Upload

### 수정 파일
- `src/main/java/com/vulsite/service/FileService.java`

### 수정 내용

**변경 전 (1차):**
```java
public FileInfo upload(MultipartFile file, User uploader) throws IOException {
    String originalName = file.getOriginalFilename();

    // 1차 수정: .jsp 확장자만 차단 (불완전)
    if (originalName != null && originalName.endsWith(".jsp")) {
        throw new IOException("JSP 파일은 업로드할 수 없습니다.");
    }
    // ...
}
```

**변경 후 (2차):**
```java
public FileInfo upload(MultipartFile file, User uploader) throws IOException {
    String originalName = file.getOriginalFilename();

    // 2차 수정: JSP 관련 확장자 블랙리스트 (불완전 - 대소문자 미처리)
    if (originalName != null) {
        String[] blockedExtensions = {".jsp", ".jspx", ".jspa", ".jspf"};
        for (String ext : blockedExtensions) {
            if (originalName.endsWith(ext)) {
                throw new IOException("JSP 관련 파일은 업로드할 수 없습니다.");
            }
        }
    }
    // ...
}
```

### 적용된 보안 대책
- **검증 방식**: 블랙리스트 기반 확장자 검사 확장
- **차단 대상**: `.jsp`, `.jspx`, `.jspa`, `.jspf`
- **검증 방법**: 배열 순회하며 확장자 확인

### 우회 방법
| 우회 파일명 | 설명 |
|------------|------|
| `shell.JsP` | 대소문자 혼합 |
| `shell.JSP` | 대문자 |
| `shell.jsp%00.jpg` | 널바이트 삽입 |

---

## 4. Path Traversal

### 수정 파일
- `src/main/java/com/vulsite/service/FileService.java`

### 수정 내용

**변경 전 (1차):**
```java
public Resource download(String filename) throws MalformedURLException {
    // 1차 수정: ../ 문자열 제거 (불완전)
    String sanitizedFilename = filename.replace("../", "");

    Path filePath = Paths.get(uploadDir).resolve(sanitizedFilename).normalize();
    // ...
}
```

**변경 후 (2차):**
```java
public Resource download(String filename) throws MalformedURLException {
    // 2차 수정: ../ 재귀적 제거 (불완전 - URL 인코딩 미처리)
    String sanitizedFilename = removePathTraversal(filename);

    Path filePath = Paths.get(uploadDir).resolve(sanitizedFilename).normalize();
    // ...
}

private String removePathTraversal(String filename) {
    String result = filename;
    while (result.contains("../")) {
        result = result.replace("../", "");
    }
    return result;
}
```

### 적용된 보안 대책
- **필터링 방식**: 재귀적 제거 (while loop)
- **필터링 대상**: `../` 문자열
- **필터링 방법**: 더 이상 `../`가 없을 때까지 반복 제거

### 우회 방법
| 우회 페이로드 | 설명 |
|--------------|------|
| `..%2f..%2fetc/passwd` | URL 인코딩 |
| `%2e%2e%2f` | 전체 URL 인코딩 |
| `..%252f` | 이중 URL 인코딩 |
| `..\..\..\etc\passwd` | Windows 백슬래시 |

---

## 5. IDOR (Insecure Direct Object Reference)

### 수정 파일
- `src/main/java/com/vulsite/controller/BoardController.java`

### 수정 내용

**변경 전 (1차):**
```java
// 수정만 작성자 확인, 삭제는 미확인

@PostMapping("/delete/{id}")
public String delete(@PathVariable Long id) {
    // 취약: 작성자 확인 없이 삭제
    boardService.delete(id);
    return "redirect:/board";
}
```

**변경 후 (2차):**
```java
// 수정/삭제 모두 작성자 확인, 조회는 미확인

@PostMapping("/delete/{id}")
public String delete(@PathVariable Long id, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/user/login";
    }

    Board board = boardService.getById(id);

    // 2차 수정: 삭제 시 작성자 확인 (불완전 - 조회는 여전히 미검증)
    if (!board.getAuthor().getId().equals(user.getId())) {
        return "redirect:/board?error=unauthorized";
    }

    boardService.delete(id);
    return "redirect:/board";
}
```

### 적용된 보안 대책
- **검증 방식**: 세션 기반 사용자 확인 확장
- **검증 대상**: 게시글 수정 + 삭제 기능
- **검증 방법**: 세션 사용자 ID와 게시글 작성자 ID 비교

### 우회 방법
| 우회 방법 | 설명 |
|----------|------|
| `GET /board/{id}` | 조회는 여전히 모든 글 접근 가능 |
| 비공개 글 열람 | 작성자가 아니어도 글 내용 확인 가능 |

---

## 6. Admin Page (Broken Access Control)

### 수정 파일
- `src/main/java/com/vulsite/controller/AdminController.java`

### 수정 내용

**변경 전 (1차):**
```java
@GetMapping("/dashboard")
public String dashboard(Model model, HttpSession session) {
    // 1차 수정: 로그인 여부만 확인 (불완전)
    if (session.getAttribute("user") == null) {
        return "redirect:/user/login";
    }
    // ...
}

@GetMapping("/config")
public String config(Model model, HttpSession session) {
    // 1차 수정: 로그인 여부만 확인 (불완전)
    if (session.getAttribute("user") == null) {
        return "redirect:/user/login";
    }
    // ...
}
```

**변경 후 (2차):**
```java
@GetMapping("/dashboard")
public String dashboard(Model model, HttpSession session) {
    // 2차 수정: 관리자 여부 확인
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/user/login";
    }
    if (!isAdmin(user)) {
        return "redirect:/?error=access_denied";
    }
    // ...
}

@GetMapping("/config")
public String config(Model model, HttpSession session) {
    // 2차 수정: 여전히 로그인만 확인 (불완전 - 관리자 확인 누락)
    if (session.getAttribute("user") == null) {
        return "redirect:/user/login";
    }
    // ...
}

private boolean isAdmin(User user) {
    return user != null && "admin".equals(user.getUsername());
}
```

### 적용된 보안 대책
- **검증 방식**: username 기반 관리자 확인
- **검증 대상**: `/dashboard`, `/users` 엔드포인트
- **검증 방법**: `isAdmin()` 메서드로 username이 "admin"인지 확인

### 우회 방법
| 우회 방법 | 설명 |
|----------|------|
| `/admin/config` 접근 | 로그인만 되면 접근 가능 |
| 평문 비밀번호 노출 | /config에서 관리자 비밀번호 확인 가능 |

---

## 참고사항

- 모든 2차 수정은 **의도적으로 불완전**하게 적용됨
- 1차 수정 대비 보안 수준은 향상되었으나 여전히 우회 가능
- 실제 운영 환경에서는 **완전한 보안 대책** 적용 필요
