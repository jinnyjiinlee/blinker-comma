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

## D2 (2026-11-02 16:50)

### 한 일
- 신호 어댑터 3종: `PleosVehicleSource`(깜빡이·기어 — road-interpreter에서 복사해 조향·문·좌석 제거, 리스너 해제 안 함) · `CarSpeedSource`(복사 + 권한 먼저 확인 — 거부면 AAOS가 조용해서) · `carEvents`(셋을 엔진 이벤트 하나로, 실패한 신호는 「확인 안 됨」 로그). 원본 저장소 변경 0.
- 낭독 서비스 `ReaderService`: MediaBrowserServiceCompat + MediaSessionCompat(제목만, 문장 본문 0) + 재생 시작 때만 포그라운드(mediaPlayback) · `AudioFocusGate`(GAIN, 줄임도 멈춤으로, 정지에서만 반납) · `EffectRunner`.
- `ReaderSession`: 엔진 상태 한 곳(메인 스레드) · `DeadlineTicker`(`nextDeadline` 1회 깨우기) · ⏸ 끄기를 실제 소리 시작까지 미룸.
- 플레이어 `ClipPlayer`·`ClipDeck`: 문장 끝 = 팩 길이 시계 · 재생마다 새 MediaPlayer(전용 스레드) · 놓기는 별도 스레드 · 다음 클립 미리 준비 · 준비 중 멈춤이면 소리 안 냄(표 재확인). 실측하며 설계를 4번 바꿈(아래 실측).
- 디버그 빌드 전용 시연 주입: `src/debug` 수신기(DUMP 권한 = adb 셸만) → 엔진 이벤트 + 화면 노란 「시연 주입」 + 로그 `CommaInject`. 릴리스 APK엔 없음.
- 임시 P 화면(▶/❙❙ · 오늘 카운트 · 자동 재개 토글 · ⏸ · 시연 주입) — 실측 손잡이용, 정식 화면은 D3.
- 측정용 합성음 팩(`tools/make_probe_pack.sh`, 사인파 8클립 2.2~5.0초) — 낭독 팩 전 임시. 실측 도구 `tools/measure_signals.py`+`measure_plans.py`+`device.py`(VC·adb·화면 누르기·주입, logcat 표지 줄).
- 문서: `docs/signals_measured.md`(정본) · SPEC 신호 표·권한·원칙 10·app 부품·위험 갱신 · ARCHITECTURE 패키지 지도·용어(시연 주입·미리 준비·소리 시작·표)·스레드 · CONVENTIONS 공통 부품·테스트 대장 · README.

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window` · 근거 `docs/logs/d2_*.txt`, 시각은 에뮬레이터 시계)
- 깜빡이 ✅ 지연 5~7ms · 1.7초 간격 토글 콜백 2개 ✅(간격 1.738초) · 기어 ✅ 7~8ms · 속도 ✅ 40km/h → 11.11m/s, 지연 141~178ms(이전 실행 최대 455ms) · 속도 0에서 레버 → `no comma` ✅ — `d2_signals_logcat.txt`.
- 핸들 키 `keyevent 85`(시연 주입) ✅ 우리 세션 수신 · 재생 중 PAUSE · 멈춤 중 PLAY · 쉼표 중(자동 재개 끔) PLAY → 같은 문장 처음부터 · 키→콜백 약 0.3초 — `d2_cut3_logcat.txt`. 컷 3 유지.
- 오디오 포커스: 다른 앱의 실제 요청 → LOSS_TRANSIENT 멈춤 → 3초 뒤 GAIN, 멈춘 2805ms부터 ✅ · 디버그 주입도 ✅ + 화면 「시연 주입」 확인.
- 권한 최소 집합: CAR_LIGHTS(깜빡이) + CAR_DRIVE(기어) + CAR_SPEED — 없으면 SecurityException(`d2_perm_none_logcat.txt` 4·39행, `d2_perm_lights_only_logcat.txt` 4행).
- 완료 콜백 ❌ 믿을 수 없음: 4.0초 클립에 4.85초(1차 실행 — 로그 덮어씀), 3.0초 클립에 2.23초(`d2_breath2_logcat.txt`). → 팩 길이 시계로, 콜백은 기록만.
- 플레이어 막힘(에뮬레이터): 일시정지한 플레이어 재사용 시 start 5~6초 · prepare 1.3~7.2초 · release 최대 7.6초 · 옛 플레이어 setVolume 8.5초(중간 실행들 — 덮어씀). 대응 뒤 최종 4개 로그 열기 25번 전부 `warm=true prepareMs=0`, 시작 0~94ms.
- 컷 1 모양 2회(문장 2 실제 소리 시작 = 0): ⓐ 2.45/2.47 · ⓑ 3.01/3.00 · ⓒ 4.19/4.22 · ⓓ 5.00/5.02(= ⓒ + 0.80) · ⏸ 꺼짐 ⓓ + 3~4ms — `d2_breath_logcat.txt`·`d2_breath2_logcat.txt`. (ⓒ가 끝 + 1.2인 건 측정 손 간격 — 실제 대본은 +1.1)
- 검사: `scripts/check.sh .` → `tests OK (app 28, engine 116)` · `check OK` — `docs/logs/d2_check.txt`.
- 확인 안 됨: 실차의 완료 콜백·시계 차이 · LEFT 레버(VC로 안 넣어 봄, 엔진은 같은 뜻) · 앱 실행 직후 메인 4초 늦음 1/4회의 원인.

### 남은 일
- 콘텐츠: 퍼블릭 도메인 영어 낭독 고르기·라이선스 원문 확인 · `tools/cut_pack.py`(문장 자르기, 끝 무음 패딩 — 시계로 끊으므로) · 실제 `pack.json` 길이로 시나리오 테스트 재실행 · 문장 A 2.6초 근처.
- D3: 정식 P·D·권한 화면(⏸ x ≥ 780dp · 속도 「확인 안 됨」 한 줄 · 권한 안내) · 테마 토큰 · 런처 아이콘. 임시 화면(`ReaderScreen`) 대체.
- D4: conductor `anchor`는 엔진 `clipStart`가 아니라 `CommaPlayer start s=N`(실제 소리 시작)에 · 녹화 전 앱 띄워 두기(첫 준비·실행 직후 지연 흡수).
- 서비스: 앱이 백그라운드일 때 핸들 키로 첫 재생(포그라운드 시작 제약) 미확인 · MediaButtonReceiver 미설치.
