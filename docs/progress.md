# 진행 기록 — 깜빡이 쉼표

단계마다 맨 아래에 「한 일 / 실측 / 남은 일」을 붙인다. 숫자는 근거 파일이 있는 것만.

## S0 (2026-11-02 16:10)

### 한 일
- 출품작 확정: 아이디어 회의 결정 정본(`../ideas/v3/meeting/DECISION.json`, M7 2026-11-02 15:30)의 MiaM22-1 「깜빡이 쉼표」 — 외부 블라인드 결선 평균 73.0 1위.
- 이름 확정(`../setup/build_runner/app.json`): slug `blinker-comma` · 표시 이름 「깜빡이 쉼표」 · 패키지 `app.blinkercomma` · 익명 팀 `CommaLane` · git 작성자 `CommaLane <blinker-comma@users.noreply.github.com>`. APK 파일명 예정 `CommaLane_깜빡이쉼표_release.apk`.
- 설계서 `docs/SPEC.md`: 한 줄 · 누가/매주/순간 · 신호 표(있음 3 · 조건부 3, 실측 줄 근거) · 원칙 11개 · 쉼표 규칙 R1~R10 · engine 부품 9종(테스트 먼저) · app 부품 · 화면 3개 · 콘텐츠 팩 · 시연 3컷 대본(VC + `anchor` 행) · MVP 20시간 배분 · 뺀 것 · 기획과 달라진 점 · 위험.
- 범위 결정 2개(개발 기준): ① 외부 앱 모드는 MVP에서 뺌(개발 계획) ② 낭독 콘텐츠는 퍼블릭 도메인 녹음을 문장 단위로 잘라 씀(카드의 「자체 녹음」 → 바뀜, 제출 서류 반영 필요).
- 저장소 `git init` + 설계 문서 커밋(코드 0).

### 실측
- 이번 단계는 에뮬레이터를 띄우지 않았다(설계만). 신호 표의 「있음」은 전부 기존 실측 로그 인용:
  - 깜빡이 4토글 4콜백 — `../ideas/v3/meeting/M6/spike_raw/logcat_turnsignal.txt` 3~6행
  - 속도 80km/h → 22.22m/s — 같은 폴더 `logcat_speed.txt` 3행
  - 속도 0 → RIGHT → 8.33m/s 순서 — `../setup/rehearsal_15_logcat_run2.txt` 108·109·112행, OFF 161행
  - 기어(서비스 수신) — `../setup/T_results_1025.md` T1 행
- 단, 위는 **이 앱이 아니라** 스파이크 프로브 앱·이전 출품작 앱이 같은 리스너로 받은 것이다. 이 앱 기준 실측은 D2부터.

### 남은 일
- D1: ride-seal 뼈대 복사 · engine(R1~R10 리듀서 · 팩 검증 · 로그 줄) 테스트 먼저 · check.sh OK.
- D2 실측 필수 3개: `-no-audio` 완료 콜백 · 핸들 `keyevent 85` 수신 · VC 1.7초 간격 콜백. 콘텐츠 원본 고르고 라이선스 원문 확인.
- 제출 서류 쪽(P1·회의): 「자체 녹음」 → 「퍼블릭 도메인 낭독」, 외부 앱 모드 → 개발 계획 표기.
- 대표 승인 대기(발송 0): 42dot 문의(램프/레버 · 자동 재개 해석).

## D1 (2026-11-02 15:57)

### 한 일
- 뼈대: 이전 출품작 저장소(ride-seal, 읽기·복사만 — 원본 변경 0)에서 Gradle 8.13 래퍼·루트/engine 빌드 스크립트·detekt·ktlint(.editorconfig)·`scripts/check.sh`(ruff 구멍 고친 판)·commit-msg 훅 복사. `check_size.py`·`check_tests.py`·CONVENTIONS 틀은 android-app 스킬 정본. 패키지 `app.blinkercomma`, 모듈 `:engine`(순수 Kotlin) ← `:app`. git 작성자 `CommaLane`, 훅 경로 `config/git-hooks`.
- `:engine` 전 부품(설계서 「engine 부품」 표): 신호 타입 · `CommaCardParams`(0.8s·2.5s·0.3m/s·자동 재개 켬) · `classifyArm` · `ReaderState`+`reduce`(R1~R10) · `Effect`/`Transition` · `ReadingTally` · 팩(`SentencePack`·`toPackFile`·`classifyPackProblems`) · 로그 줄(`ReaderLog`).
- 시연 대본 3개 `demo/script/cut1_breath.tsv`·`cut2_wait_go.tsv`·`cut3_key_resume.tsv` + 시나리오 테스트(대본을 그대로 읽어 「앱 흉내」 리플레이 — 클립 길이로 ClipEnded, 마감 시각에 Tick, 신호는 대본 줄에서만). 숨은 조작 3개마다 「빠뜨리면 깨진다」 테스트.
- `:app`: 빈 `MainActivity`(앱 이름만) + Pleos 값 변환(`toTurnSignal`·`toGear`) — assembleDebug 통과.
- 문서: `docs/ARCHITECTURE.md`(구조·용어 사전·대본 형식) · `CONVENTIONS.md`(공통 부품·테스트 대장) · `README.md`. 커밋 3개(3557cac·5d7aa68·c2fda92).

### 설계서와 달라진 점 (SPEC에 반영함)
- `Tick`: 100ms 폴링 → 앱이 `ReaderState.nextDeadline(params)` 시각에 1회. 폴링이면 로그 `OFF held`가 0.80~0.89초로 흔들려 ⓓ 글자가 대본과 안 맞는다.
- 컷 3 기대 로그 `mediaKey PLAY_PAUSE → play` → `mediaKey PLAY → play`: keyevent 85(PLAY_PAUSE)는 MediaSession 기본 처리가 멈춤 중이면 onPlay로 바꿔 준다(문서 근거 — 에뮬 수신은 D2 실측).
- 대본 시각: 컷 2·3의 고정 초(4.0 등) → 「anchor 문장 끝 X초 전」(`end-X`). 고정 초면 문장 길이에 따라 예약 뒤 2.5초 상한에 걸려 문장 중간에서 멈춘다.
- `armedAt`은 `Reading` 안, `offSince`·`HoldReason`은 `Holding` 안(단계 밖에선 뜻이 없어서). 팩 문제에 `BLANK_CREDIT`(크레딧 없음) 추가. `log/ToLogLine.kt` → `ReaderLog.kt`+`LogFormat.kt`.
- 엔진이 재생 명령마다 `clipStart s=N` 줄을 낸다(대본 anchor용 — 설계서 보조 줄에 없던 것).

### 실측
- 이번 단계는 에뮬레이터를 띄우지 않았다(엔진·뼈대만). 차 신호·플레이어 실측 0건 — 전부 D2.
- 검사: `scripts/check.sh .` → `size OK` · `tests OK (app 12, engine 116)` · `check OK` — 근거 `docs/logs/d1_check.txt`. (app 12 = 테스트 6개 × debug·release)
- 커버리지(:engine, Kover XML): 줄 248/254 = 97.6% · 분기 155/170 = 91.2% (바닥 90%·85%) — 근거 `docs/logs/d1_kover.txt`(줄) · 분기는 `engine/build/reports/kover/report.xml`에서 읽음(빌드 산출물이라 커밋 안 함).
- `./gradlew :app:assembleDebug` 성공(app-debug.apk 생성).
- 시나리오 테스트의 시각(컷 1 ⓐ2.0 ⓑ2.6 ⓒ3.7 ⓓ4.5)은 **합성 테스트 팩**(`engine/src/test/resources/stage_pack.json`, 문장 A = 2.6초로 맞춘 가짜 길이) 기준 엔진 리플레이 값이다 — 실제 낭독·실제 콜백 시각 아님.

### 남은 일
- D2: 신호 어댑터 3종(깜빡이·기어·속도) · ClipPlayer · ReaderService(MBS+MediaSession+포그라운드, `nextDeadline` 타이머) · AudioFocusGate · 팩 자르기(`tools/cut_pack.py`) · 권한 최소 집합 실측.
- D2 실측 필수: `-no-audio` 완료 콜백 · `keyevent 85` 수신(→ 엔진엔 onPlay로 오는지) · VC 1.7초 간격 콜백 2개 → `docs/signals_measured.md`.
- 실제 팩이 생기면 시나리오 테스트를 실제 `pack.json` 길이로 다시 돌린다. 컷 1은 문장 A가 3.1초 이하여야 ⓓ가 5초 안(ⓓ = A + 1.9초) — 2.6초 근처 문장을 A로 고른다.
- 콘텐츠 원본(퍼블릭 도메인 낭독) 선정·라이선스 원문 확인.
