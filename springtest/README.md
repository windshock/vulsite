# Spring Vulnerable Site (vulsite)

취약점 학습 및 보안 진단 실습을 위한 Spring 기반 취약 웹 애플리케이션

## 목적

- 웹 애플리케이션 보안 취약점 이해
- 모의해킹 실습 환경 제공
- 시큐어코딩 학습

> **경고**: 이 프로젝트는 교육 목적으로만 사용해야 합니다. 실제 운영 환경에 배포하지 마세요.

---

## 구현 취약점 목록

| No | 취약점 | 설명 | OWASP |
|----|--------|------|-------|
| 1 | SQL Injection | 사용자 입력값이 SQL 쿼리에 직접 삽입되어 DB 조작 가능 | A03:2021 |
| 2 | Stored XSS | 악성 스크립트가 DB에 저장되어 다른 사용자에게 실행됨 | A03:2021 |
| 3 | File Upload | 확장자/MIME 검증 없이 파일 업로드 허용 (웹쉘 업로드 가능) | A04:2021 |
| 4 | File Download | 경로 조작을 통한 임의 파일 다운로드 가능 (Path Traversal) | A01:2021 |
| 5 | 버전 정보 노출 | 에러 페이지에서 서버/프레임워크 버전 정보 노출 | A05:2021 |
| 6 | Tomcat 기본 페이지 | Tomcat 기본 페이지 및 관리 콘솔 노출 | A05:2021 |
| 7 | 평문 계정 정보 | 설정 파일 내 관리자 계정/비밀번호 평문 저장 | A02:2021 |
| 8 | 관리자 페이지 접근 | 인증 없이 관리자 페이지 URL 직접 접근 가능 | A01:2021 |
| 9 | 타사용자 정보 수정 | 권한 검증 없이 다른 사용자 정보 수정 가능 (IDOR) | A01:2021 |
| 10 | 타사용자 글 삭제 | 권한 검증 없이 다른 사용자 게시글 삭제 가능 (IDOR) | A01:2021 |

---

## 기술 스택

- **Backend**: Spring Boot 2.x
- **Database**: MySQL / H2
- **Template Engine**: Thymeleaf / JSP
- **Build Tool**: Gradle / Maven
- **Server**: Apache Tomcat

---

## 프로젝트 구조

```
springtest/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/vulsite/
│   │   │       ├── controller/
│   │   │       │   ├── UserController.java      # 사용자 관련 (SQLi, IDOR)
│   │   │       │   ├── BoardController.java     # 게시판 (XSS, IDOR)
│   │   │       │   ├── FileController.java      # 파일 업/다운로드
│   │   │       │   └── AdminController.java     # 관리자 페이지
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       └── config/
│   │   └── resources/
│   │       ├── application.properties           # 평문 계정 정보
│   │       └── templates/
│   └── test/
├── build.gradle
└── README.md
```

---

## 취약점 상세

### 1. SQL Injection
```java
// 취약한 코드 예시
String query = "SELECT * FROM users WHERE id = '" + userId + "'";
```
- **위치**: `UserController.java`
- **공격 예시**: `' OR '1'='1`

### 2. Stored XSS
```java
// 취약한 코드 예시
model.addAttribute("content", userInput); // 이스케이프 없이 출력
```
- **위치**: `BoardController.java`
- **공격 예시**: `<script>alert('XSS')</script>`

### 3. File Upload
```java
// 취약한 코드 예시
file.transferTo(new File(uploadPath + file.getOriginalFilename()));
```
- **위치**: `FileController.java`
- **공격 예시**: `.jsp` 웹쉘 업로드

### 4. File Download (Path Traversal)
```java
// 취약한 코드 예시
String filePath = "/uploads/" + request.getParameter("filename");
```
- **위치**: `FileController.java`
- **공격 예시**: `../../etc/passwd`

### 5. 에러 페이지 버전 정보 노출
```properties
# 취약한 설정
server.error.include-stacktrace=always
server.error.include-message=always
```

### 6. Tomcat 기본 페이지 노출
- `/manager/html` 접근 가능
- 기본 404/500 에러 페이지에서 Tomcat 버전 노출

### 7. 설정 파일 내 평문 계정 정보
```properties
# application.properties
admin.username=admin
admin.password=admin123!@#
db.password=root1234
```

### 8. 관리자 페이지 직접 접근
```java
// 인증 체크 없음
@GetMapping("/admin/dashboard")
public String adminPage() {
    return "admin/dashboard";
}
```

### 9. 타사용자 정보 수정 (IDOR)
```java
// 권한 검증 없이 사용자 ID로 직접 수정
@PostMapping("/user/update")
public String updateUser(@RequestParam Long userId, UserDto dto) {
    userService.update(userId, dto);
}
```

### 10. 타사용자 글 삭제 (IDOR)
```java
// 작성자 확인 없이 삭제
@DeleteMapping("/board/{id}")
public String deletePost(@PathVariable Long id) {
    boardService.delete(id);
}
```

---

## 실행 방법

```bash
# 1. 프로젝트 클론
git clone https://github.com/windshock/vulsite.git
cd vulsite/springtest

# 2. 빌드 및 실행
./gradlew bootRun

# 3. 브라우저 접속
http://localhost:8080
```

---

## 참고 자료

- [OWASP Top 10 (2021)](https://owasp.org/Top10/)
- [OWASP Testing Guide](https://owasp.org/www-project-web-security-testing-guide/)
- [CWE (Common Weakness Enumeration)](https://cwe.mitre.org/)

---

## 면책 조항

이 프로젝트는 **교육 및 연구 목적**으로만 제공됩니다. 이 코드를 사용하여 발생하는 모든 법적 책임은 사용자 본인에게 있습니다. 허가 없이 타인의 시스템에 대해 이 기술을 사용하는 것은 불법입니다.
