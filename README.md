# 한자 학습 엔진

자바로 구현하는 한자 학습 엔진(콘솔).
- 한자 JSON 로드 → 문제 생성(뜻/음/섞기)
- 30초 카운트다운 타이머 + exit로 중도 종료
- 세션 종료 시 오답 요약 일괄 출력
- 출제 규칙은 전략(Strategy)으로 교체 가능

## 기능 목록 (초안)
- 데이터
    - [x] `src/main/resources/hanja-data.json`에서 한자 목록 로드
    - [x] `Hanja`, `HanjaRepository`, `JsonHanjaRepository` 구현
- 퀴즈
    - [x] `QuestionType`(HANJA_TO_MEANING / MEANING_TO_HANJA / HANJA_TO_READING)
    - [x] `Quiz`(문제 텍스트/채점)
    - [x] `QuizStrategy` 인터페이스 + `RandomQuizStrategy`
    - [x] `MistakeFirstStrategy` (오답 우선 출제)
- 학습 진행
    - [x] `LearningRecord`, `UserProgress`(정답/오답 기록, 재출제 여부 판정)
    - [x] `StudyService`
- 저장(영속)
    - [x] `ProgressRepository` 인터페이스
    - [x] `FileProgressRepository`(JSON/텍스트 저장)
- UI
    - [x] 인터랙티브 시작 프롬프트: 급수(복수 선택)/문항 수/모드(뜻·음·섞기)
    - [x] 문제당 30초 타이머 + 카운트다운 표시
    - [x] `exit` 입력 시 즉시 종료
- 테스트
    - [x] `JsonHanjaRepository` 로딩 테스트
    - [x] `UserProgress.recordResult / needsReview` 테스트
    - [x] `RandomQuizStrategy` 기본 동작 테스트
    - [x] `MistakeFirstStrategy` 오답 우선
- 이후 확장(옵션)
    - [ ] 급수/주제별 필터, 일일 목표(OKR) 로그 출력

---

## 프로그래밍 요구 사항
- [x] 기능을 구현하기 전 **README.md에 구현할 기능 목록**을 정리해 추가한다.
- [x] **커밋 단위**는 README의 기능 목록 단위로 쪼갠다.
- [x] **JDK 21**에서 실행 가능해야 한다. (Gradle toolchain으로 고정)
- [ ] **자바 코드 컨벤션** 준수 (Java Style Guide 기준)
    - [ ] **인덴트(depth) ≤ 2** 유지
    - [ ] **3항 연산자 미사용**
- [x] **JUnit 5 + AssertJ**로 기능 테스트 검증

---

## 디렉터리 구조 (초안)
    hanja
    ├─ domain/    (Hanja, HanjaRepository, JsonHanjaRepository)
    ├─ quiz/      (Quiz, QuestionType, QuizStrategy, RandomQuizStrategy, MistakeFirstStrategy)
    ├─ study/     (LearningRecord, UserProgress, StudyService)
    ├─ storage/   (ProgressRepository, FileProgressRepository)
    ├─ util/      (AnswerNormalizer, AsyncLineReader, TimedPrompt)                  // 입력/카운트다운 유틸
    └─ ui/
        ├─ ConsoleApp.java                                         // 오케스트레이션
        ├─ ConsolePrompter.java                                    // 초기 프롬프트
        ├─ config/  (SessionConfig)                                // 세션 설정 값
        ├─ session/ (SessionRunner, Attempt, SessionResult)        // 세션 실행/결과
        └─ view/    (ReviewPrinter)                                // 오답 요약 출력

---

## 데이터 포맷 (JSON)
src/main/resources/hanja-data.json

    [
    { "character": "一", "reading": "일", "meaning": "하나", "level": "8급" },
    { "character": "二", "reading": "이", "meaning": "둘",  "level": "8급" },
    { "character": "人", "reading": "인", "meaning": "사람", "level": "8급" }
    ]

    필드: character(한자), reading(독음), meaning(뜻), level(급수)

---

## 실행
    ```bash
    ./gradlew run

### 흐름

1. 급수 입력 → 예: 8급 또는 7급,8급 (비우면 전체)
2. 문항 수 → 기본 20
3. 모드 선택 → [1] 뜻 / [2] 음(독음) / [3] 섞어서
4. 풀이 중: 한 문제당 30초 카운트다운 표시, exit로 중도 종료
5. 세션 종료 시 오답 요약 출력 → 정답/내 답/급수/문항 유형

- Windows 터미널: 한글 깨짐 시 chcp 65001 후 실행.