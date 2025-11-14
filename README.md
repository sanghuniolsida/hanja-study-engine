# 한자 학습 엔진

자바로 구현하는 한자 학습 엔진(콘솔).
- 한자 JSON을 로드해 문제(한자→뜻/뜻→한자/한자→음) 생성
- 정답/오답 진행도 저장(로컬 파일)
- 출제 규칙은 전략(Strategy)으로 교체 가능

## 기능 목록 (초안)
- 데이터
    - [ ] `src/main/resources/hanja-data.json`에서 한자 목록 로드
    - [ ] `Hanja`, `HanjaRepository`, `JsonHanjaRepository` 구현
- 퀴즈
    - [ ] `QuestionType`(HANJA_TO_MEANING / MEANING_TO_HANJA / HANJA_TO_READING)
    - [ ] `Quiz`(문제 텍스트/채점)
    - [ ] `QuizStrategy` 인터페이스 + `RandomQuizStrategy`
- 학습 진행
    - [ ] `LearningRecord`, `UserProgress`(정답/오답 기록, 재출제 여부 판정)
    - [ ] `StudyService`(오늘 문제 N개 생성)
- 저장(영속)
    - [ ] `ProgressRepository` 인터페이스
    - [ ] `FileProgressRepository`(JSON/텍스트 저장, 폴더 자동 생성)
- UI
    - [ ] `ConsoleApp`: 문제 N개 출제 → 입력 → 채점 → 저장 → 요약 출력
- 테스트
    - [ ] `JsonHanjaRepository` 로딩 테스트
    - [ ] `UserProgress.recordResult / needsReview` 테스트
    - [ ] `RandomQuizStrategy` 기본 동작 테스트
- 이후 확장(옵션)
    - [ ] `MistakeFirstStrategy`(오답 우선 출제)
    - [ ] 급수/주제별 필터, 일일 목표(OKR) 로그 출력

---

## 프로그래밍 요구 사항
- [ ] 기능을 구현하기 전 **README.md에 구현할 기능 목록**을 정리해 추가한다.
- [ ] **커밋 단위**는 README의 기능 목록 단위로 쪼갠다.
- [ ] **JDK 21**에서 실행 가능해야 한다. (Gradle toolchain으로 고정)
- [ ] **자바 코드 컨벤션** 준수 (Java Style Guide 기준)
    - [ ] **인덴트(depth) ≤ 2** 유지
    - [ ] **3항 연산자 미사용**
- [ ] **JUnit 5 + AssertJ**로 기능 테스트 검증

---

## 디렉터리 구조 (초안)
    hanja
    ├─ domain/ (Hanja, HanjaRepository, JsonHanjaRepository)
    ├─ quiz/   (Quiz, QuestionType, QuizStrategy, RandomQuizStrategy)
    ├─ study/  (LearningRecord, UserProgress, StudyService)
    ├─ storage/(ProgressRepository, FileProgressRepository)
    └─ ui/     (ConsoleApp)

## 실행
```bash
./gradlew run

