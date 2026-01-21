# B담당 보안 기대 테스트 리포트

## 개요
- 대상: B담당 취약점 (#3 File Upload, #4 Path Traversal, #8 Admin Direct Access)
- 테스트 목적: “보안이 적용되었을 때” 기대되는 동작을 검증
- 결과: 현재 구현은 취약하게 동작하므로 모든 테스트가 실패 (의도된 실패)

## 실행 환경
- JDK: 11
- 명령: `JAVA_HOME=$(/usr/libexec/java_home -v 11) gradle test --no-daemon`

## 실패 요약
- 총 3개 테스트 실패 (보안 기대 동작 미충족)

## 실패 상세

### 1) 위험한 파일 업로드 차단 실패 (취약점 #3)
- 테스트명: `보안 기대: 위험한 확장자/MIME 업로드는 차단되어야 함`
- 기대: `.jsp` 업로드 요청이 4xx로 차단되고 DB에도 저장되지 않음
- 실제: 업로드가 허용됨
- 관련 파일:
  - `springtest/src/main/java/com/vulsite/service/FileService.java`
  - `springtest/src/main/java/com/vulsite/controller/FileController.java`

### 2) Path Traversal 차단 실패 (취약점 #4)
- 테스트명: `보안 기대: Path Traversal 시도는 차단되어야 함`
- 기대: `../secret.txt` 접근이 4xx로 차단됨
- 실제: 파일 접근이 가능함
- 관련 파일:
  - `springtest/src/main/java/com/vulsite/service/FileService.java`
  - `springtest/src/main/java/com/vulsite/controller/FileController.java`

### 3) 관리자 페이지 인증 미검증 (취약점 #8)
- 테스트명: `보안 기대: 관리자 페이지는 인증 없이 접근 불가해야 함`
- 기대: 미인증 접근 시 `/user/login`으로 리다이렉트
- 실제: 인증 없이 접근 가능
- 관련 파일:
  - `springtest/src/main/java/com/vulsite/controller/AdminController.java`

## 참고
- 테스트 코드는 `springtest/src/test/java/com/vulsite/BVulnerabilityTests.java`
- 상세 실패 내용은 Gradle 테스트 리포트에서 확인 가능:
  - `springtest/build/reports/tests/test/index.html`
