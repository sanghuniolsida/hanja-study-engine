# 한자 학습 엔진

자바로 구현한 한자 학습 도메인 엔진입니다.
- 한자 JSON을 로드해 문제(한자→뜻/뜻→한자/한자→음) 생성
- 정답/오답 진행도 저장(파일)
- 출제 규칙은 전략(Strategy)으로 교체 가능

## Stack
- Java 21, Gradle (Kotlin DSL)
- Jackson (JSON 파싱), JUnit 5

## Run
```bash
./gradlew run
