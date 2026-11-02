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

## D3 (2026-11-02 17:11)

### 한 일
- 화면 3개(설계서 「화면 목록」 D3 확정판으로 갱신): **P 화면**(정차 — 앱 표지·사용법 세 줄·오늘 카운트·자동 재개 줄·출처 / 오른쪽 판: 팩 제목·재생 원 버튼·지금 단계·「확인 안 됨」 줄) · **D 화면**(주행 — 빈 화면, 쉼표 중 ⏸ 96dp 하나) · **권한 안내**(첫 실행 정차 중 한 번). D2 임시 화면(`ReaderScreen`) 삭제.
- 어느 화면인지는 순수 함수 `ScreenInputs.toUiState`: 기어 D·R·N 또는 (기어를 몰라도) 속도 > 0.3m/s면 D 화면, 주행 중엔 권한 안내도 정차까지 미룸, 기어·속도 못 받으면 P 화면에 「○ … 신호 확인 안 됨」 줄.
- 테마 토큰 `ui/theme`(색·글자·간격·크기·모서리 + 대비 계산) — 강조색은 깜빡이 호박색 하나, 어두운 바탕, 애니메이션 0. 화면 부품 9개 `ui/component`(목록 ARCHITECTURE 8절).
- 런처 아이콘: 적응형(어두운 타일 + 호박색 쉼표) — 매니페스트에 `android:icon`이 없어 기본 안드로이드 아이콘이 뜨던 것 고침. 알림 아이콘도 같은 쉼표.
- 속도 권한: `SpeedPermissionStore`(허용·안 물음·거부) + 허용되는 순간 속도 구독 시작(`carEvents`가 권한 흐름을 기다림 — Pleos 리스너는 그대로, 원칙 9).
- 고친 결함 2개(캡처·실행으로 발견): ① 문자열 이름 `state_on`/`state_off`가 Compose 라이브러리 이름과 겹쳐 한국어 기기에서 「켬」이 「사용」으로 뜸 → `toggle_state_*`로 ② 쉼표 중 P 원 버튼이 「정지」(PausePressed)라 「자동 재개 끔」일 때 P의 ▶로 재개(R8)가 안 됨 → 소리가 안 나면 ▶(`toScreenPress`, 테스트 4개).
- 실측 도구: D 화면엔 누를 것이 없어 `tools/measure_plans.py`의 화면 누르기를 전부 P(기어 Park)로 옮김 · `device.tap_text`가 그림 버튼 이름(content-desc — 「재생」·「정지」)도 찾게.
- 문서: SPEC(화면 목록·원칙 11 실측값·기획과 달라진 점 1줄) · ARCHITECTURE(패키지 지도·용어 4개·8절 화면 부품) · CONVENTIONS(공통 부품·테스트 대장 4줄) · README.

### 실측 (에뮬레이터 `-no-audio -no-window`, 캡처는 저장소 밖 `../setup/build_shots/D3_*.png` 13장)
- 앱 창 = 화면 [853,90]~[2535,1300] → **1682×1210dp**(160dpi) — `dumpsys window` 실측.
- 측후방 카메라(깜빡이 ON 때 시스템이 띄움) = 앱 창 왼쪽 위 **754×424dp** — `D3_06_drive_hold.png` 픽셀 측정. ⏸는 앱 창 x 1197~1263dp · y 564~646dp(카메라 오른쪽 끝 754dp보다 443dp 오른쪽) — 같은 캡처.
- 속도 권한 시스템 창 문구 = 「깜빡이 쉼표에서 이 기기의 **위치 정보**에 액세스하도록 허용하시겠습니까?」(`D3_02_permission_dialog.png`) → 권한 안내 화면에 「시스템 창에는 「위치 정보」 권한으로 보여요」 한 줄 추가. 허용 뒤 `CAR_SPEED: granted=true`, P 화면에 「확인 안 됨」 줄 없음(`D3_03`). 권한 회수 뒤엔 「○ 속도 신호 확인 안 됨」(`D3_09`).
- 컷 흐름 화면: P 재생 → VC 기어 Drive·속도 40 → D 빈 화면(`D3_05`) → BlinkRight → `clipEnd s=1 lever=ON → hold` + ⏸(`D3_06`) → Off → `OFF held 0.80s → play s=2` → 포커스 주입 → D 화면 「시연 주입」(`D3_07`).
- 「자동 재개 끔」 → 쉼표 → 레버 OFF(`autoResume=off → keep hold`) → 정차 → P 화면 「쉼표 중」 + ▶ → 탭 → `screen PLAY → play s=2` — `docs/logs/d3_park_resume_logcat.txt`(`D3_12`·`D3_13`).
- 잘림·겹침·고아 단어: 13장 눈으로 확인 — 없음(가장 긴 줄 「낭독 출처 · 측정용 합성음(낭독 팩 교체 전)」도 한 줄).
- 검사: `scripts/check.sh .` → `tests OK (app 92, engine 116)` · `check OK` — `docs/logs/d3_check.txt`.
- 확인 안 됨: 실차 화면 크기·카메라 크기(에뮬레이터 값만) · 햇빛 아래 실제 가독성(대비는 계산 모델 `CommaThemeFloorTest`만) · Pleos 홈 미디어 카드에 우리 세션이 안 뜸(재생 중에도 「재생 중인 항목 없음」 — 캡처 `D3_06` 왼쪽 아래, 원인 미조사).

### 남은 일
- D4: conductor `anchor`(=`CommaPlayer start s=N`) · 대본 3컷 녹화 2회 동일성 · README 규정 4항목. 대본 준비 줄의 「P에서 ▶」는 D 화면에 버튼이 없으니 반드시 기어 D 전에.
- 낭독 팩(퍼블릭 도메인) 들어오면: P 화면 팩 제목·출처 줄 다시 캡처(제목 ≤6자 · 출처 길이 한 줄 확인).
- Pleos 홈 미디어 카드에 우리 MediaSession이 안 뜨는 이유 조사(원칙 6 메타 제목 확인이 이 카드에서 돼야 함).
- 스토어·기획서용 스크린샷은 디버그 「시연 주입」 없는 컷(`D3_03`·`D3_06`·`D3_10`)에서 고른다.

## D4 (2026-11-02 17:37)

### 한 일
- 시연 대본 자동 재생 `demo/`: `conductor.py`(한 컷 — road-interpreter conductor를 복사해 고쳐 씀, 원본 변경 0) · `script_rows.py`(대본 읽기 · `end-X` 계산) · `log_watch.py`(logcat 받기 — 시각은 에뮬레이터 시계) · `rehearsal.py`(새 설치 → 권한 안내 [허용] → 시스템 창 「허용」 → 컷 1·2·3, `adb shell screenrecord` 녹화) · `rehearsal_summary.py`(컷별 「첫 anchor부터 몇 초에 무슨 줄」 + 두 리허설 비교, 같음 = 줄 순서·글자 같고 시각 차이 ≤ 0.15초).
  - anchor 「문장 시작」은 엔진 `clipStart`가 아니라 `CommaPlayer start s=N`(실제 소리 시작), `end-X`의 문장 길이는 그 줄의 `clipMs − fromMs`.
  - 대본 줄마다 로그에 `CommaStage` 표지(컷 경계 · anchor · 늦음 ms) — 앱 로그와 한 파일에서 맞춰 본다.
  - 컷 사이엔 앱을 다시 띄워 매 컷 문장 1부터(측정용 팩 8문장 끝에 닿지 않게) — 띄우기 전에 차를 P로(D 화면엔 「재생」이 없다).
- 고친 결함 3개(리허설·녹화로 발견):
  1. **준비 줄 간격** — 1초×3이면 ▶ 뒤 준비가 첫 문장(2.6초)을 넘겨 anchor가 한 문장 밀림(1회차 첫 시도: 문장 A가 4.0초짜리 s=3 → ⓓ 5.94초, 5초 밖 — 그 로그는 다시 돌린 1회차로 덮어씀). → 준비 줄 사이 0.5초, 마지막 줄 뒤 대기 없음.
  2. **다음 문장이 2.8~3.3초 늦게 시작**(2·3·4회차 컷 2) — 에뮬레이터에서 놓기(release)·준비(prepare)가 가끔 수 초 막히는데, 미리 준비가 소리 내는 스레드에서 돌아 다음 문장 열기가 그 뒤에 줄 섰다. 1차 수정(`PlayerHandoff` — 끝난 문장 플레이어 놓기를 다음 준비 뒤로)으로 놓기 막힘은 사라졌지만 준비 자체가 5.9~7.6초 막혀 그대로였다 → 2차 수정 `ClipStandby`: 준비를 자기 스레드로 떼고 두 문장 앞까지 준비. 테스트 `PlayerHandoffTest` 5개.
  3. **앱을 띄우면 P 화면에 「○ 기어 신호 확인 안 됨」이 잠깐 떴다 사라짐**(1회차 녹화 33초) — 첫 콜백 전 null을 「못 받음」으로 그렸다. → 화면을 띄운 뒤 3초는 「아직 모름」(줄 숨김), 그 뒤에도 null이면 그대로 표시(원칙 4 유지). 테스트 `ScreenInputsTest` 1개.
- 검사 범위: `scripts/check.sh`의 ruff에 `demo/` 추가.
- README를 공모전 GitHub 규정 4항목(앱 이름·한 줄 소개 / 실행 방법 / 테스트 환경·설정 / APK 위치) 형식으로 다시 씀(실명 0).
- 문서: ARCHITECTURE(패키지 지도 `demo/`·player 부품 · 용어 「놓기」·「리허설」) · CONVENTIONS(공통 부품 · 테스트 대장 2줄) · SPEC(위험 절).

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window` · 디버그 APK · 측정용 합성음 팩 · 근거 `docs/logs/d4_rehearsal_0N_logcat.txt`, 녹화는 저장소 밖 `../setup/build_shots/rehearsal_0N.mp4`)
- 리허설 6회(1·2 = 고치기 전 · 3·4 = 1차 수정 · 5·6 = 최종). 매회 새 설치 → 권한 → 3컷, 녹화 84.0~87.8초(180초 상한 안).
- **최종 5·6회차 연속 같음**(`rehearsal_summary.py` → 「같음(차이 ≤ 0.15s)」). 6회차 시각(첫 anchor = 문장 A 실제 소리 시작 = 0):
  - 컷 1(문장 A = s=2, 3.0초): ⓐ `turnSignal=RIGHT`·`moving → comma armed` 2.41 · ⓑ `clipEnd s=2 lever=ON → hold`·⏸ 켜짐 3.00 · ⓒ `turnSignal=OFF` 4.11 · ⓓ `OFF held 0.80s → play s=3`·⏸ 꺼짐 4.91(5.0초 안).
  - 컷 2: RIGHT 1.01 `lever=ON speed=0 → no comma` · 다음 문장 3.01 · 속도 30 → `moving → comma armed` 6.15 · hold 7.01 · OFF 7.53 · ⓓ 8.33.
  - 컷 3: RIGHT 2.01 · hold 3.00 · OFF 4.02 `autoResume=off → keep hold` · 핸들 키(시연 주입) → `mediaKey PLAY → play s=3` 6.37(대본 키 시각 6.00 → 콜백 약 0.37초, adb 입력 포함) · P 8.51.
- 1·2회차 차이: 2회차 컷 2에서 문장 3 소리가 엔진 명령보다 2.76초 늦음(`openMs=2761`) — 직전 놓기 `release ms=5636`. 1·2회차 놓기 17번 중 5번이 2.2~5.6초.
- 3·4회차(1차 수정): 놓기는 전부 53ms 이하로 바뀌었지만 준비가 5.9~7.6초 막혀 3회차 컷 2 `openMs=3281`, 4회차 컷 2 `openMs=3000`.
- 5·6회차(최종): 준비 막힘 6번(2768~8064ms)이 그대로 났는데도 열기 31번 전부 준비된 플레이어(`warm=false` 0번), 최대 `openMs=27`.
- 앱 실행 → 첫 기어 콜백(엔진 `gear=` 줄): 1.03~2.07초(3~6회차 16번) → 화면 첫 신호 대기 3초의 근거.
- 녹화 프레임 검사(1회차 3초 간격 30장 + 구간 2fps, 6회차 2초 간격 44장 — 앱 창만 잘라 봄): 권한 안내 → 시스템 창 → P(오늘 0·0) → D 빈 화면 → 측후방 카메라 + ⏸ → P(2·1 → 6·2 → 9·3, 실제 카운트) · 「자동 재개 끔」 토글 확인. 6회차엔 「기어 신호 확인 안 됨」 깜빡임 없음.
- 컷 사이 흰 화면 약 1~2초: 앱 강제 종료 뒤 Pleos가 앱 자리를 #F7F7F7로 비워 두는 것(시작 창 없음 — 종료 직후·실행 직후 캡처 3장 같은 색). 리허설이 컷마다 앱을 다시 띄워서 생기는 것이고 앱 화면이 아니다.
- 검사: `scripts/check.sh .` → `tests OK (app 104, engine 116)` · `check OK` — `docs/logs/d4_check.txt`.
- 확인 안 됨: 실차·소리 켠 에뮬레이터에서의 준비·놓기 막힘(`-no-audio`에서만 잼) · 낭독 팩 길이에서의 컷 시각.

### 남은 일
- 낭독 팩(퍼블릭 도메인)이 들어오면 리허설 2회 다시 — 컷 1 문장 A는 2.6초 근처(지금 측정용 팩 3.0초로 ⓓ 4.91초, 여유 0.09초).
- 영상 촬영(11/10)은 컷별로 따로 녹화·편집(리허설의 컷 사이 재실행 흰 화면은 영상에 안 씀). 영상 자막 「시연 주입」(컷 3 핸들 키) · 「소리는 편집 합성」.
- D5: 서명 릴리스 APK `apk/CommaLane_깜빡이쉼표_release.apk`(키스토어는 저장소 밖) → README 4항 갱신.
- Pleos 홈 미디어 카드에 우리 세션이 안 뜨는 이유(D3에서 넘어옴).
