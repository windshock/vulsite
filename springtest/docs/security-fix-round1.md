# 취약점 1차 수정 보고서

**작성일**: 2025-01-26
**수정 단계**: 1차 (불완전한 수정)
**목적**: 보안 학습을 위한 단계별 취약점 패치 및 우회 방법 학습

---

## 수정 요약

| 취약점 | 수정 파일 | 보안 대책 | 우회 가능 여부 |
|--------|----------|----------|---------------|
| XSS | BoardService.java | `<script>` 태그 필터링 | O |
| SQL Injection | UserService.java | 싱글쿼트(') 제거 | O |
| File Upload | FileService.java | .jsp 확장자 차단 | O |
| Path Traversal | FileService.java | ../ 문자열 제거 | O |
| IDOR | BoardController.java | 수정 시 작성자 확인 | O |
| Admin Page | AdminController.java | 로그인 여부 확인 | O |

---

## 1. XSS (Cross-Site Scripting)

### 수정 파일
- `src/main/java/com/vulsite/service/BoardService.java`

### 수정 내용

**변경 전:**
```java
public Board create(String title, String content, User author) {
    Board board = new Board(title, content, author);
    return boardRepository.save(board);
}

public Board update(Long id, String title, String content) {
    Board board = boardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    board.setTitle(title);
    board.setContent(content);
    board.setUpdatedAt(LocalDateTime.now());
    return boardRepository.save(board);
}
```

**변경 후:**
```java
public Board create(String title, String content, User author) {
    // 1차 수정: <script> 태그만 필터링 (불완전)
    String filteredTitle = title.replaceAll("<script>", "").replaceAll("</script>", "");
    String filteredContent = content.replaceAll("<script>", "").replaceAll("</script>", "");

    Board board = new Board(filteredTitle, filteredContent, author);
    return boardRepository.save(board);
}

public Board update(Long id, String title, String content) {
    Board board = boardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

    // 1차 수정: <script> 태그만 필터링 (불완전)
    String filteredTitle = title.replaceAll("<script>", "").replaceAll("</script>", "");
    String filteredContent = content.replaceAll("<script>", "").replaceAll("</script>", "");

    board.setTitle(filteredTitle);
    board.setContent(filteredContent);
    board.setUpdatedAt(LocalDateTime.now());
    return boardRepository.save(board);
}
```

### 적용된 보안 대책
- **필터링 방식**: 블랙리스트 기반
- **필터링 대상**: `<script>`, `</script>` 태그 문자열
- **필터링 방법**: `String.replaceAll()`로 빈 문자열로 치환

### 우회 방법
| 우회 페이로드 | 설명 |
|--------------|------|
| `<img src=x onerror=alert(1)>` | 이미지 태그 이벤트 핸들러 |
| `<svg onload=alert(1)>` | SVG 태그 이벤트 핸들러 |
| `<body onload=alert(1)>` | body 태그 이벤트 핸들러 |
| `<iframe src="javascript:alert(1)">` | iframe javascript 프로토콜 |

---

## 2. SQL Injection

### 수정 파일
- `src/main/java/com/vulsite/service/UserService.java`

### 수정 내용

**변경 전:**
```java
public User loginVulnerable(String username, String password) {
    String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
    List<User> users = entityManager.createNativeQuery(query, User.class).getResultList();
    return users.isEmpty() ? null : users.get(0);
}
```

**변경 후:**
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

### 적용된 보안 대책
- **필터링 방식**: 블랙리스트 기반
- **필터링 대상**: 싱글쿼트(`'`) 문자
- **필터링 방법**: `String.replace()`로 빈 문자열로 치환

### 우회 방법
| 우회 페이로드 | 설명 |
|--------------|------|
| `admin\' OR 1=1--` | 백슬래시로 이스케이프 |
| `admin%27 OR 1=1--` | URL 인코딩 (앱에서 디코딩 시) |
| `admin%bf%27 OR 1=1--` | 멀티바이트 인코딩 |

### 참고
- 근본적으로 문자열 연결 방식의 쿼리 생성은 취약함
- PreparedStatement 또는 파라미터 바인딩 사용이 올바른 해결책

---

## 3. File Upload

### 수정 파일
- `src/main/java/com/vulsite/service/FileService.java`

### 수정 내용

**변경 전:**
```java
public FileInfo upload(MultipartFile file, User uploader) throws IOException {
    String originalName = file.getOriginalFilename();
    String storedName = System.currentTimeMillis() + "_" + originalName;
    // ... 검증 없이 저장
}
```

**변경 후:**
```java
public FileInfo upload(MultipartFile file, User uploader) throws IOException {
    String originalName = file.getOriginalFilename();

    // 1차 수정: .jsp 확장자만 차단 (불완전)
    if (originalName != null && originalName.endsWith(".jsp")) {
        throw new IOException("JSP 파일은 업로드할 수 없습니다.");
    }

    String storedName = System.currentTimeMillis() + "_" + originalName;
    // ...
}
```

### 적용된 보안 대책
- **검증 방식**: 블랙리스트 기반 확장자 검사
- **차단 대상**: `.jsp` 확장자
- **검증 방법**: `String.endsWith()`로 확장자 확인

### 우회 방법
| 우회 파일명 | 설명 |
|------------|------|
| `shell.jspx` | JSP XML 형식 |
| `shell.jspa` | 대체 JSP 확장자 |
| `shell.jspf` | JSP Fragment |
| `shell.JSP` | 대소문자 우회 (OS에 따라) |
| `shell.jsp.jpg` | 이중 확장자 |

---

## 4. Path Traversal

### 수정 파일
- `src/main/java/com/vulsite/service/FileService.java`

### 수정 내용

**변경 전:**
```java
public Resource download(String filename) throws MalformedURLException {
    Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
    Resource resource = new UrlResource(filePath.toUri());

    if (resource.exists()) {
        return resource;
    }
    throw new RuntimeException("파일을 찾을 수 없습니다: " + filename);
}
```

**변경 후:**
```java
public Resource download(String filename) throws MalformedURLException {
    // 1차 수정: ../ 문자열 제거 (불완전)
    String sanitizedFilename = filename.replace("../", "");

    Path filePath = Paths.get(uploadDir).resolve(sanitizedFilename).normalize();
    Resource resource = new UrlResource(filePath.toUri());

    if (resource.exists()) {
        return resource;
    }
    throw new RuntimeException("파일을 찾을 수 없습니다: " + sanitizedFilename);
}
```

### 적용된 보안 대책
- **필터링 방식**: 블랙리스트 기반
- **필터링 대상**: `../` 문자열
- **필터링 방법**: `String.replace()`로 빈 문자열로 치환

### 우회 방법
| 우회 페이로드 | 설명 |
|--------------|------|
| `....//....//etc/passwd` | 중첩 패턴 (필터 후 `../../etc/passwd`) |
| `..%2f..%2fetc/passwd` | URL 인코딩 |
| `%252e%252e%252f` | 이중 URL 인코딩 |
| `..\..\etc\passwd` | Windows 백슬래시 |

---

## 5. IDOR (Insecure Direct Object Reference)

### 수정 파일
- `src/main/java/com/vulsite/controller/BoardController.java`

### 수정 내용

**변경 전:**
```java
@GetMapping("/edit/{id}")
public String editForm(@PathVariable Long id, Model model, HttpSession session) {
    if (session.getAttribute("user") == null) {
        return "redirect:/user/login";
    }
    Board board = boardService.getById(id);
    model.addAttribute("board", board);
    return "board/edit";
}

@PostMapping("/edit/{id}")
public String edit(@PathVariable Long id,
                   @RequestParam String title,
                   @RequestParam String content) {
    boardService.update(id, title, content);
    return "redirect:/board/" + id;
}

@PostMapping("/delete/{id}")
public String delete(@PathVariable Long id) {
    boardService.delete(id);
    return "redirect:/board";
}
```

**변경 후:**
```java
@GetMapping("/edit/{id}")
public String editForm(@PathVariable Long id, Model model, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/user/login";
    }

    Board board = boardService.getById(id);

    // 1차 수정: 수정 폼 접근 시 작성자 확인
    if (!board.getAuthor().getId().equals(user.getId())) {
        return "redirect:/board?error=unauthorized";
    }

    model.addAttribute("board", board);
    return "board/edit";
}

@PostMapping("/edit/{id}")
public String edit(@PathVariable Long id,
                   @RequestParam String title,
                   @RequestParam String content,
                   HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/user/login";
    }

    Board board = boardService.getById(id);

    // 1차 수정: 수정 처리 시 작성자 확인
    if (!board.getAuthor().getId().equals(user.getId())) {
        return "redirect:/board?error=unauthorized";
    }

    boardService.update(id, title, content);
    return "redirect:/board/" + id;
}

// 삭제는 여전히 작성자 확인 없음 (취약)
@PostMapping("/delete/{id}")
public String delete(@PathVariable Long id) {
    boardService.delete(id);
    return "redirect:/board";
}
```

### 적용된 보안 대책
- **검증 방식**: 세션 기반 사용자 확인
- **검증 대상**: 게시글 수정 기능 (GET, POST)
- **검증 방법**: 세션의 사용자 ID와 게시글 작성자 ID 비교

### 우회 방법
| 우회 방법 | 설명 |
|----------|------|
| `POST /board/delete/{id}` | 삭제 기능은 작성자 확인 없음 |
| 타인 글 삭제 | 게시글 ID만 알면 누구나 삭제 가능 |

---

## 6. Admin Page (Broken Access Control)

### 수정 파일
- `src/main/java/com/vulsite/controller/AdminController.java`

### 수정 내용

**변경 전:**
```java
@GetMapping("/dashboard")
public String dashboard(Model model) {
    // 취약: 인증 체크 없음
    model.addAttribute("users", userService.findAll());
    model.addAttribute("boards", boardService.findAll());
    model.addAttribute("files", fileService.findAll());
    model.addAttribute("adminUsername", adminUsername);
    model.addAttribute("adminPassword", adminPassword);
    return "admin/dashboard";
}

@GetMapping("/users")
public String userList(Model model) {
    // 취약: 인증 체크 없음
    model.addAttribute("users", userService.findAll());
    return "admin/users";
}

@GetMapping("/config")
public String config(Model model) {
    // 취약: 인증 체크 없음
    model.addAttribute("adminUsername", adminUsername);
    model.addAttribute("adminPassword", adminPassword);
    return "admin/config";
}
```

**변경 후:**
```java
@GetMapping("/dashboard")
public String dashboard(Model model, HttpSession session) {
    // 1차 수정: 로그인 여부만 확인 (불완전 - 관리자 권한 미확인)
    if (session.getAttribute("user") == null) {
        return "redirect:/user/login";
    }

    model.addAttribute("users", userService.findAll());
    model.addAttribute("boards", boardService.findAll());
    model.addAttribute("files", fileService.findAll());
    model.addAttribute("adminUsername", adminUsername);
    model.addAttribute("adminPassword", adminPassword);
    return "admin/dashboard";
}

@GetMapping("/users")
public String userList(Model model, HttpSession session) {
    // 1차 수정: 로그인 여부만 확인 (불완전)
    if (session.getAttribute("user") == null) {
        return "redirect:/user/login";
    }

    model.addAttribute("users", userService.findAll());
    return "admin/users";
}

@GetMapping("/config")
public String config(Model model, HttpSession session) {
    // 1차 수정: 로그인 여부만 확인 (불완전)
    if (session.getAttribute("user") == null) {
        return "redirect:/user/login";
    }

    model.addAttribute("adminUsername", adminUsername);
    model.addAttribute("adminPassword", adminPassword);
    return "admin/config";
}
```

### 적용된 보안 대책
- **검증 방식**: 세션 기반 로그인 확인
- **검증 대상**: 모든 관리자 페이지 (/dashboard, /users, /config)
- **검증 방법**: 세션에 "user" 속성 존재 여부 확인

### 우회 방법
| 우회 방법 | 설명 |
|----------|------|
| 일반 사용자 로그인 | 아무 계정으로 로그인 후 /admin/* 접근 |
| 권한 우회 | role/권한 확인 없이 로그인만 되면 접근 가능 |
| 평문 비밀번호 노출 | /admin/config에서 관리자 비밀번호 확인 가능 |

---

## 참고사항

- 모든 1차 수정은 **의도적으로 불완전**하게 적용됨
- 보안 학습 목적으로 **우회 방법을 함께 제공**
- 실제 운영 환경에서는 **완전한 보안 대책** 적용 필요
