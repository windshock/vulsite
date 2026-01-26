# 보안 퍼징 검증 가이드

**작성일**: 2025-01-26  
**목적**: 취약점 보안 조치의 우회 가능성을 퍼징으로 검증

---

## 1. 대상 및 범위

- **대상 함수**: `BoardService#filterXss(String)` (private)
- **테스트 방식**: Jazzer(JUnit) + Jsoup 파싱 검증
- **검증 기준**: 필터 처리 후 HTML 태그(요소)가 남지 않아야 함

---

## 2. 테스트 구성

### 테스트 파일
- `src/test/java/com/vulsite/fuzz/XssFilterFuzzTest.java`

### 검증 로직
1. 리플렉션으로 `filterXss` 호출
2. Jsoup로 결과 HTML 파싱
3. `body` 자식 요소가 있으면 실패

---

## 3. 실행 방법

```bash
# 단일 테스트 (regression 모드)
/tmp/gradle-8.5/gradle-8.5/bin/gradle test --tests "com.vulsite.fuzz.XssFilterFuzzTest"

# 실제 fuzzing 모드
JAZZER_FUZZ=1 /tmp/gradle-8.5/gradle-8.5/bin/gradle test --tests "com.vulsite.fuzz.XssFilterFuzzTest" --rerun-tasks
```

---

## 4. 결과 확인

- 실패 시 Assertion 메시지에 **남은 태그 목록**과 **파싱된 HTML**이 포함됨
- Jazzer는 재현용 입력을 `springtest/` 루트에 `crash-<hash>` 파일로 저장

---

## 5. 주의사항

- `JAZZER_FUZZ=1` 환경변수 없이 실행하면 회귀 테스트 모드로 동작
- 퍼징 입력은 바이너리일 수 있으므로, 필요 시 `xxd`로 확인
- 테스트가 실패하는 것은 **우회 입력 발견**을 의미
