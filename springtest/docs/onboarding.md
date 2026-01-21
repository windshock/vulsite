# Vulsite 온보딩 가이드 (주니어 개발자용)

이 문서는 **취약점 성공/실패**를 목표로 하는 것이 아니라, 이 프로젝트에 사용된 **기술 요소**를 이해하는 것을 목표로 합니다.

---

## 1. 프로젝트 구조 이해

### 핵심 패키지 구조

- `controller/`  
  HTTP 요청을 받고 응답을 반환하는 레이어
- `service/`  
  비즈니스 로직을 처리하는 레이어
- `repository/`  
  DB 접근을 담당하는 레이어 (JPA)
- `entity/`  
  DB 테이블과 매핑되는 모델 클래스
- `resources/templates/`  
  화면 렌더링용 Thymeleaf 템플릿

### 시작점

- `VulsiteApplication.java`  
  Spring Boot 앱의 엔트리 포인트

---

## 2. 주요 기술 요소 요약

### Spring Boot
- 애플리케이션 부트스트랩과 컴포넌트 스캔
- 의존성 주입(@Autowired, @RequiredArgsConstructor)

### Spring MVC
- `@Controller` / `@GetMapping` / `@PostMapping`
- `Model`을 이용한 화면 데이터 전달

### JPA (H2)
- `Entity` ↔ 테이블 매핑
- `Repository` 인터페이스 기반 CRUD

### Thymeleaf
- 서버 사이드 렌더링 템플릿
- `th:text`, `th:href`, `th:each` 활용

### Session 인증
- 로그인 후 `HttpSession`에 `user` 저장
- 각 컨트롤러에서 로그인 여부 확인

### 파일 업로드/다운로드
- `MultipartFile` 업로드
- `UrlResource` 다운로드
- 정적 리소스 매핑 (`WebConfig`)

### 테스트
- JUnit 5 + MockMvc (API/컨트롤러 테스트)
- Playwright (브라우저 E2E 테스트)

---

## 3. 코드 읽기 순서 (추천)

1. **로그인 흐름**
   - `UserController.login()`
   - `UserService.loginVulnerable()`
   - `UserRepository`

2. **게시판 흐름**
   - `BoardController.write()`, `BoardController.detail()`
   - `BoardService`
   - `BoardRepository`

3. **파일 업로드/다운로드**
   - `FileController.upload()`, `FileController.download()`
   - `FileService.upload()`, `FileService.download()`

4. **관리자 페이지**
   - `AdminController.dashboard()`

5. **테스트 코드**
   - `BVulnerabilityTests` (MockMvc 기반)
   - `StoredXssTest` (Playwright 기반)

---

## 4. 테스트 실행 흐름 이해

### 단위 테스트/컨트롤러 테스트
- `MockMvc`를 사용해 HTTP 요청을 시뮬레이션
- 실제 서버 구동 없이 동작

### 브라우저 테스트
- Playwright로 실제 브라우저를 띄워 UI 흐름 검증
- 로그인 → 게시글 작성 → 화면 렌더링 흐름 확인

---

## 5. 학습 체크리스트

- [ ] Controller → Service → Repository 흐름 설명 가능
- [ ] Entity와 DB 매핑 구조 설명 가능
- [ ] Session이 어디서 생성/사용되는지 설명 가능
- [ ] 파일 업로드/다운로드 동작 설명 가능
- [ ] MockMvc와 Playwright 차이 설명 가능

---

## 6. 추천 실습 루틴 (1~2시간)

1. 로그인 코드 흐름 따라가기  
2. 게시판 글 작성 후 DB 저장 확인  
3. 파일 업로드 경로 확인  
4. MockMvc 테스트 1개 실행  
5. Playwright 테스트 1개 실행  

---

## 7. 참고 경로

- 코드 기준: `springtest/src/main/java/com/vulsite`
- 템플릿: `springtest/src/main/resources/templates`
- 테스트: `springtest/src/test/java/com/vulsite`
