# 깜빡이 쉼표 (Blinker Comma)

깜빡이를 올리면 낭독이 그 문장 끝에서 숨을 참고, 레버가 돌아오면 다음 문장 — 화면도 손도 없이. Pleos Connect 차량용 앱.

## 지금 상태
- `:engine` 완료: 쉼표 예약 판정 · 낭독 상태 리듀서(쉼표 규칙 R1~R10) · 로그 줄 · 오늘 카운트 · 콘텐츠 팩 검사 · 시연 대본 3컷 리플레이
- `:app`: 빈 화면 + Pleos 값 변환만. 신호 어댑터·플레이어·서비스·화면은 다음 단계
- 테스트 수: `scripts/check.sh .`의 `tests OK (…)` 숫자 — 현재 `app 12, engine 116` (app은 debug·release 단위 테스트가 각각 돌아 2배로 셈)

## 처음 실행
```
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # JDK 21 (25는 Gradle 8.13과 안 맞음)
git config core.hooksPath config/git-hooks          # 클론 후 1회 — fix: 커밋은 테스트와 같이
./gradlew check                                       # ktlint·detekt(타입 분석)·테스트·커버리지·Lint
scripts/check.sh .                                    # + 파일 크기·블라인드·테스트 실행 수
./gradlew :app:assembleDebug
```

## 먼저 읽을 것
1. docs/SPEC.md — 원칙 11개 · 쉼표 규칙 R1~R10
2. docs/ARCHITECTURE.md — 구조·용어 사전(이 단어만 씀)·대본 형식
3. CONVENTIONS.md — 숫자 상한·공통 부품·테스트 대장

## 시연 주입 신호
- 핸들 미디어 키: Virtual Console에 카드가 없어 `adb shell input keyevent 85`로 넣는다(컷 3, 영상 밖). 대본 `demo/script/cut3_key_resume.tsv`의 `adb` 줄.

## 꼭 지킬 것
- 블라인드 심사: 커밋 작성자·코드·주석·문서에 실명·회사명·로컬 경로·전화번호 금지
- 키스토어·비밀값은 저장소 밖
