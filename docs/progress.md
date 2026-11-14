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

## M1 (2026-11-02 17:55)

### 한 일
- 검수 담당(Mason): 새로 설치 → 시연 대본 리허설 3회(`demo/rehearsal.py`) · SPEC 원칙 11개 실측 재확인 · check.sh · engine 경계 케이스 · 첫 실행·권한 거부·재시작 · 실명 grep(코드·커밋·리소스). 코드 수정 0, 결과는 `docs/qa_M1.md`.

### 실측
- `scripts/check.sh .` 통과(`tests OK (app 104, engine 116)`).
- 리허설 3회 중 2회차 재현 실패 — 컷 1 준비의 VC 속도 40 PUT이 200을 받았는데도 차 속도가 끝까지 0으로 남아 쉼표가 안 걸림(엔진은 R3대로 옳게 반응). 로그는 QA 작업 스크래치패드(저장소 밖)의 `qa_run1~3_logcat.txt` — 세션 종료로 사라지므로 재현 필요하면 같은 명령으로 다시 돌릴 것. 자세한 내용·증거 줄은 `docs/qa_M1.md` 결함 1.
- 콘텐츠 팩이 여전히 측정용 합성음(`da4da3d` 이후 교체 커밋 없음), `CREDITS.txt` 없음 — `docs/qa_M1.md` 결함 2.
- engine 경계 케이스 1건 새로 확인: 팩 마지막 문장이 레버 ON(예약 중)으로 끝나면 `OnClipEnded.kt`가 R1(쉼표 멈춤)보다 `finish()`(팩 종료)를 먼저 타 쉼표 없이 조용히 멈춤 — `docs/qa_M1.md` 결함 3.
- 실명·회사명 grep(코드·strings.xml·CREDITS·커밋 작성자·커밋 메시지) 전부 0건, 커밋 작성자 `CommaLane` 단일.
- 첫 실행→권한 안내→허용 거부→P 화면 「확인 안 됨」→재시작해도 권한 재노출 0, 오늘 카운트 유지 확인.
- 기존에 「미확인」으로 남아 있던 두 항목 확인: 앱이 백그라운드(홈)로 나간 상태에서 핸들 키 첫 재생 정상 동작 / Pleos 홈 미디어 카드 쪽 `GleoMediaSession`에 재생 중 `title=측정 팩`로 잡히는 로그 확인(화면 캡처로는 재확인 안 함, 결론 보류).

### 남은 일
- 결함 1(VC 속도 명령 가끔 무반응)·결함 3(팩 끝+레버ON 경계) 수정 — 둘 다 코드는 안 고쳤고 재현만 남김.
- 결함 2(콘텐츠 팩 교체)는 기존에 추적 중이던 항목 — 11/8 시연 동결 전 반드시.
- D5 서명 릴리스 APK는 그대로 남은 일(M1 범위 밖으로 봄).

## D5 (2026-11-02 18:31)

### 한 일
- **결함 3 수정**(engine, `fix:`): `OnClipEnded.kt`가 팩 끝(`finish`)을 레버 예약(`armedAt`)보다 먼저 검사해 마지막 문장+레버 ON이면 쉼표 없이 조용히 멈추던 것 → 예약 검사를 먼저 하도록 순서를 바꿈. 재개할 다음 문장이 없으면 처음(index 0)으로. `ReaderClipTest`에 버그 재현 테스트 2개 + 재개 테스트 1개 추가(기존 2개는 「예약 없이」로 이름 바꿔 유지 — 그 경로는 그대로 맞음).
- **결함 1 대응**(demo 도구, `feat:` — 앱 코드 버그가 아니라 VC 쪽 산발적 드롭이라 앱을 못 고침): `demo/conductor.py`에 속도 PUT 확인·재시도(`set_speed`) 추가 — PUT 뒤 앱 로그의 `SpeedChanged`를 0.7초 기다렸다가 안 보이면 같은 PUT을 최대 2번 더 보낸다. 1차 구현은 "이미 목표 속도라 새 이벤트가 안 오는" 정상 케이스(컷 사이 `reset_car()` 직후)를 결함으로 오판해 리허설이 멈췄다 → `latest_speed_mps`로 전체 로그에서 마지막 값을 먼저 보고, 이미 같으면 재시도 없이 한 번만 보내도록 고침.
- **결함 2 수정**(콘텐츠 팩 교체): LibriVox "The Aesop for Children" 섹션 13(archive.org `aesopforchildren_1308_librivox`, Public Domain Mark 1.0 — 메타데이터 `licenseurl` 확인)을 원본으로 받아(`tools/raw/`, 커밋 안 함) `tools/cut_pack.py`(신규)로 잘랐다. 설계서는 ffmpeg 무음 구간 검출을 적었지만 실제로는 word-level STT(whisper, 모델 small, `--word_timestamps`) 타임스탬프로 문장 끝(.!?)을 찾는 쪽으로 바꿨다 — 무음 검출보다 자막이 실제 발화와 안 어긋난다(SPEC 반영). 인트로·아웃트로 문장은 빼고(`--skip-start 1 --skip-end 1`) 오인식 2곳만 교정(`--fix`, 뜻은 안 바꿈: 「PECOCK」→「PEACOCK」, 「his evil lost」→「is evil lost」). 38문장·176.7초. `app/src/main/assets/packs/en1/pack.json`·`s01~s38.m4a`(기존 포맷과 동일 mono 22050Hz AAC 64k)·`CREDITS.txt`(출처 URL·sha256·라이선스·자르기 절차) 교체. `tools/README.md`(원본 데이터셋 1줄) 신규.
- **릴리스 서명**: `app/build.gradle.kts`에 `signingConfigs`/`buildTypes.release` 추가 — 저장소 밖 `~/.blinker-comma/keystore.properties`가 있을 때만 서명(없으면 미서명 빌드, 커밋 0). 키스토어(`~/.blinker-comma/release.keystore`, PKCS12, DN에 실명 0)도 이번에 새로 만듦.
- **(되돌림) 릴리스 로그 상시 켜기**: `CarEvents.kt`의 `BuildConfig.DEBUG` 가드를 처음엔 걷어냈다(릴리스 APK로도 conductor 결함1 확인이 되게) → 보안 검토에서 "릴리스에서 속도·기어를 logcat에 상시 남기면 ADB 접근자에게 주행 행태가 샐 수 있다" 지적을 받고 즉시 되돌림. 결과: conductor의 속도 확인·재시도는 **디버그 APK에서만** 동작(원래부터 디버그 전용 도구이고 11/10 실촬영도 디버그 APK로 한다 — 문제 없음). 릴리스 APK는 리허설 대본 대신 설치→실행→권한 흐름만 손으로 확인.
- `status_for_form.md` 신규 — 기능별 구현 완료(에뮬레이터 실측)/시연 주입/개발 계획 표.
- README·SPEC·ARCHITECTURE 갱신(콘텐츠 팩 상태·APK 위치·테스트 수 119·cut_pack 방식).
- **블라인드 위반 발견·조치**: `docs/qa_M1.md`의 결함 3번 줄이 블라인드 단어 목록(이름·회사명·이메일)을 grep 대상으로 그대로 타이핑해 놔서 `scripts/check.sh`의 블라인드 검사가 걸렸다. 작업 트리는 고쳤지만 **이미 커밋된 과거 커밋(94eb272, M1) 안에는 그 줄이 그대로 남아 있다** — push는 안 했지만(원칙상 금지), 제출 전 이 커밋을 포함한 히스토리 정리(스쿼시 또는 재작성)가 필요하다. 되돌리기 쉽지 않은 작업이라 이번엔 건드리지 않고 여기 기록만 한다. (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio`, 근거 `docs/logs/d5_*.txt`)
- `scripts/check.sh .` → `tests OK (app 104, engine 119)` · `check OK`(블라인드 검사 포함 통과).
- `./gradlew :app:assembleRelease` 성공 → `apksigner verify` v2 스킴 통과(v1/v3/v3.1/v4는 안 씀 — 기본 AGP 설정).
- 디버그 APK로 리허설 1회(새 설치→권한→3컷) 전부 성공, 속도 PUT 재시도 0회 — `docs/logs/d5_rehearsal_logcat.txt`, 녹화 저장소 밖 `../setup/build_shots/d5_rehearsal.mp4`.
- 릴리스 APK: 새 설치 → 권한 안내 → [허용] → 시스템 창 허용 → P 화면까지 수동 확인, 크래시 로그(FATAL/AndroidRuntime) 0건 — `../setup/build_shots/d5_02_release_launch.png`(권한 안내) · `d5_03_release_park.png`(P 화면, 새 콘텐츠 팩 제목·출처 표시).
- P 화면 캡처(`../setup/build_shots/d5_01_park_realpack.png`): 「낭독 1과」· 「1 / 38 문장」· 「낭독 출처 · The Aesop for Children — LibriVox(CC0)」 한 줄, 잘림·겹침 없음(A6 검사).
- 콘텐츠 팩 38클립 durationMs가 실제 파일 길이(ffprobe)와 일치 확인(샘플 s01·s14·s37·s38).

### 남은 일
- 과거 커밋(94eb272)의 블라인드 위반 — 제출 전 히스토리 정리 필요(대표 확인 뒤 처리). (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)
- 콘텐츠 팩 교체로 컷 1 영상 시각(⏸ ⓐ~ⓓ)이 바뀜(문장 A=s02, 2.63초) — 11/10 실촬영 대본 재검토.
- 시나리오 테스트(`engine/src/test/resources/stage_pack.json`)는 여전히 합성 길이 — 실제 팩 길이로 바꾸는 건 CONVENTIONS 테스트 대장에 미작성으로 남아 있음(이번 범위 밖).
- 42dot 문의(레버 자동 복귀·자동 재개 해석) 여전히 대표 승인 대기.

## M2 (2026-11-02 19:02)

### 한 일
- 최종 검수 담당(Mason): qa_M1 결함 3건 재현 절차로 전부 재확인(코드 수정 0, 문서만 커밋) · release APK(`apk/`) 새 설치 검증 · 시연 대본 리허설 3회(요구 2회 + 컷2 재현성 확인용 1회, 디버그 APK) · `status_for_form.md` 19행 전수 대조. 결과 `docs/qa_M2.md`.

### 실측
- `scripts/check.sh .` → `tests OK (app 104, engine 119)` · `check OK`.
- 결함 1(VC 속도 무반응): 리허설 3연속 재현 절차 — 3/3 성공, 재발 0(스크래치패드 `qa_m2_defect1_run1~3_logcat.txt`, 세션 종료로 사라짐).
- 결함 2(콘텐츠 팩): `pack.json`·`CREDITS.txt`·release APK 화면 캡처로 실제 낭독 38문장 팩 확인, probe 문자열 0건.
- 결함 3(팩 끝+레버 ON 경계): **실제 38문장 팩으로 재현** — s=38 재생 중 BlinkRight ON → `clipEnd s=38 lever=ON → hold` 정상 발생(qa_M1 때의 "pack end → stop" 재발 0) → OFF 0.8초 뒤 `play s=1`로 재개 확인.
- **새 발견(결함 4, 중)**: 리허설 3회 교차비교(`rehearsal_summary.py`, 1v2·2v3·1v3)에서 컷 2(cut2_wait_go)의 `turnSignal=OFF` 도착 시각이 매회 0.3~0.5초씩 어긋남(D4 동일성 기준 ≤0.15초 초과, 3비교 전부). conductor 자체 `late=`는 정상(4~11ms) — VC 신호 전달 쪽 문제로 보임, 결함 1과 같은 계열이나 turn-signal PUT엔 재시도 로직이 없음. 코드 수정 안 함, `docs/qa_M2.md`에 재현 절차·근거 기록.
- release APK: `apksigner verify` v2 통과·DN 실명 0, `application-debuggable` 없음 확인. 새 설치→권한→P 화면 크래시 0, `CommaSignal` 태그에 신호 값 로그 0건(정적 대기 문구 1줄만) — e71b2b8 되돌림이 release 빌드에 실제 반영됨을 재확인.
- 시연 리허설 최종본: `setup/build_shots/rehearsal_final.mp4`(84.45초, 저장소 밖). 컷 1 영상 시각(ⓐ2.0·ⓑ2.64·ⓒ3.7~3.8·ⓓ4.5~4.6초, 3회 평균)이 SPEC 목표와 거의 일치 — D5가 남긴 "컷 1 시각 재검토" 숙제는 이 결과로 닫아도 됨.
- 블라인드 히스토리 위반(94eb272): `git show 94eb272 -- docs/qa_M1.md`로 재확인 — **여전히 미해결**, 이번에도 손대지 않음(대표 확인 필요, 되돌리기 어려운 작업). (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)
- `status_for_form.md` 19행 대조 — 18행 실제와 일치, 1행(시연 대본 자동 재생)은 기능상 맞지만 결함 4 캐치업 참고 필요(표 자체는 수정 불필요로 판단).

### 남은 일
- 결함 4(컷 2 VC 신호 drift) — conductor `play_vc`에도 확인·재시도 추가 검토(적용은 개발 담당, 11/10 전).
- 94eb272 블라인드 히스토리 정리 — 대표 승인 대기, 11/15 제출 전 필수. (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)
- 그 외는 progress.md D5 「남은 일」과 동일(42dot 문의, stage_pack.json 실제 길이 전환 등 — 이번 범위 밖).

## P1 (2026-11-02 19:30)

### 한 일
- 제출 패키지 `submit/blinker-comma_제출본/`(저장소 밖, `hyundai-app-contest/submit/`) 신규 생성. 코드 변경 0 — 이 저장소(`blinker-comma/`)는 읽기만 했다.
- `submit/build_form_blinker-comma.py`(`build_form_seal.py` 복사해 내용만 교체) → 접수양식 01~04를 담은 docx·pdf 1벌. 01·04는 운영용 실명(기존 두 제출본과 같은 관례, 블라인드 대상 아님), **02는 블라인드**(성명·연락처·이메일·소속·로컬 경로 grep 0 — 02 섹션만 분리 추출해 재확인, 전체 문서 기준으로도 재확인). 5번 「개발 계획」은 `docs/status_for_form.md` 표를 그대로 삽입. 과장 문구(유일·최초·완벽 등) grep 0. 7쪽 전체 PNG 캡처로 잘림·침범 육안 확인(이상 없음). 04 서명 칸은 비워 둠(대표 몫).
- 시연 영상: `setup/build_shots/rehearsal_final.mp4`(이 저장소 밖, M2 최종 리허설 84.45초)에 타이틀 카드(앱 이름 + 「실제 소리 없음 · 소리는 편집 합성, 시각은 앱 로그 기준」 고지)·엔드 카드(스토어 한 줄)를 이어 붙여 91.47초로 제작. 실명·로컬 경로 0(프레임 샘플 7장 육안 확인).
- GitHub 업로드용 소스 zip: `git archive HEAD`로 생성 — `.git`·`local.properties`·키스토어·`*.jks` 0건(zip 내용 grep으로 확인). APK는 git 추적 파일이라 zip 안에도 포함되고, 별도 사본도 제출본 폴더에 둠.
- `mail_body.md`(존댓말, 수신 appdevchallenge@hyundai-partners.com, 제목 규정 그대로)·`CHECKLIST.md`(필수 항목 완료 표시 + 대표 몫 📌 6개 + 위험 4개) 작성. **메일 발송·GitHub push·외부 업로드는 하지 않음**(원칙대로).

### 실측
- 접수양식 02 섹션 블라인드 grep(android-app 스킬 금지어 목록 — 대표 성명·회사명·대표 이메일·휴대폰번호·로컬 절대경로 패턴) 전부 0건(전체 문서 기준 재확인 포함) — `submit/깜빡이쉼표_접수양식_v1.docx`.
- 과장 문구(유일·최초·완벽·세계 최초·업계 최초) grep 0건.
- PDF 7쪽 전수 캡처(100dpi PNG) 육안 확인 — 잘림·표 침범·빈 쪽 0건.
- 시연 영상 1차 시도(오버레이 자막 합성)는 인코딩이 12분을 넘기며 `moov atom not found`로 손상(ffprobe duration이 5483초로 깨짐) — 폐기. 타이틀 카드에 고지 문구를 포함시키고 `concat` demuxer(`-c copy`)로 재시도해 91.47초 정상 파일 확보(ffprobe 확인).
- 소스 zip `unzip -l` grep: `.git/`·`local.properties`·키스토어·`*.jks` 0건.

### 남은 일 (제출 전 — 전부 대표 몫, CHECKLIST.md에 상세)
- 04 서명 칸 채우기.
- 익명 GitHub 계정 생성 + push — **이 저장소를 그대로 push하지 말 것**: 과거 커밋 `94eb272`에 블라인드 금지어가 여전히 남아 있음(위 D5·M2 기록). push 전 히스토리 정리 필수. 제출용 zip 자체는 `git archive`라 이 문제와 무관. (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)
- mail_body.md 검토 후 발송 승인(용량 문제로 메일엔 접수양식+영상만, 소스 zip·APK는 GitHub 링크로 전달하도록 본문 구성).
- 메일 제목 — 개인 참가 규정 문구("팀명(개인은 성명)_대표자명")를 그대로 적용하면 대표 성명이 두 번 반복되는 모양이 됨. 그대로 둘지 확인 요청(`mail_body.md` 참고).
- 42dot 문의·가출원 여부는 기존처럼 승인 대기(이번 단계에서 새로 만들지 않음).

## U0 (2026-11-02 20:20)

### 한 일
- 대표 지시(11/2 20시) 「허전한 부분은 UI/UX 최대한 개선해서 이쁘게 해」 대응 — **이번 단계는 감사·설계만, 코드 수정 0**.
- `~/.claude/skills/frontend-design/SKILL.md` · `docs/ARCHITECTURE.md` 8절(화면 부품) · `research/deep_v3/platform.md`(NHTSA 2초/12초·Pleos DO 허용 범위) 읽고 전 화면 비평.
- 에뮬레이터(`-no-audio -no-window`)에서 실제 낭독 팩 기준 전 상태 재실측·재캡처(`setup/build_shots/U0_01~10_*.png`, 저장소 밖): 권한 안내·P 대기·P 낭독·D 낭독(빈 화면)·D 쉼표 LEFT/RIGHT·재개 직후·속도 신호 확인 안 됨·런처.
- 차량 HMI 디자이너 시각 비평 + 개선 목록(우선순위 P0~P2)·화면별 와이어(글로)·토큰 변경안(`CommaSize`·`CommaColors` 신규 상수안)·애니메이션 명세(시간·곡선)·안전 근거를 `docs/ui_plan.md`로 작성·커밋.
- 핵심 제안: ① D 쉼표에 레버 방향과 같은 쪽에서 들어오는 1회성(비반복) 전환 ② D 읽는 중 화면에 정적 워터마크(애니메이션 없음) ③ P 화면 `Spacer(weight=1f)` 과다 정리 + 오늘 쉼표 타임라인. SPEC 원칙 1 "애니메이션 0"을 "지속 루프 0 + 사건 1회성 전환 허용"으로 재해석하는 안을 포함 — **이 재해석은 구현 전 대표 승인 필요**로 문서에 명시.
- "큰 원형/파형으로 낭독 진행 표시"는 D(주행) 화면이 아니라 P(정차) 화면 한정 + 정적 세그먼트(연속 회전 없음)로만 구현 권장 — 연속 루프는 주변시야 모션 포착 위험이 있어 보류 권장으로 결론(2절 P2).
- 야간/주간 대비 테마는 이번 라운드에서 신규 구현하지 않기로 결론 — NIGHT_MODE는 SPEC이 채택한 신호 목록 밖이라 새 신호 승인·실측이 필요해 11/8 동결 일정과 안 맞음. 기존 `Contrast.kt`의 `flare`(햇빛 반사) 모델이 밝은 환경 대비 4.5 이상을 이미 보장.

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window`, 디버그 APK, 실제 낭독 팩)
- **신규 발견**: 레버 LEFT와 RIGHT를 둘 다 실측 — 측후방 카메라 오버레이 위치·⏸ 위치가 **완전히 동일**함을 확인(`U0_06_drive_hold_right.png` vs `U0_07_drive_hold_left.png` 픽셀 비교). 기존 D3 실측은 RIGHT만 했었음(진행 기록 D3 「실측」 절 — 대본이 전부 BlinkRight만 썼기 때문).
- VC 속도 PUT 산발적 무반응(qa_M1 결함1과 같은 계열) 재확인 — 같은 값으로 1~2회 재시도하면 반영됨, conductor.py는 이미 재시도 로직 있음(D5 대응), 수동 curl 호출엔 재시도가 없어 이번 실측 중 지연 발생(기록만, 코드 영향 없음).
- `scripts/check.sh .` → `tests OK (app 104, engine 119)` · `check OK`(커밋 전 재확인).
- 확인 안 됨: 실제 카메라 오버레이가 실차에서도 레버 방향과 무관하게 항상 왼쪽에 뜨는지(에뮬레이터만 실측, T_results_1025에도 방향별 비교 없음) — 다음 라운드에서 실차 자료나 Pleos 설명회 질의로 교차확인 필요.

### 남은 일
- 대표 승인 대기: ui_plan.md 4절(애니메이션 재해석), 제안 전체(P0~P2 중 어디까지 이번 라운드에 넣을지).
- 승인되면 U1: 토큰 추가 → `HoldGlyph` 방향성 전환 → `CommaWatermark`·`TodayTimeline`·`ProgressRing` 신규 부품 → 전 상태 재캡처·잘림 검사 → `CommaThemeFloorTest`류 좌표·대비 재검증 → 컷 1·2·3 영상 시각이 새 전환(260+180ms) 포함해도 5초 상한 안인지 D4식 리허설 2회 재확인 → `check.sh`.

## U1 (2026-11-02 20:55)

### 한 일
- 대표 지시(11/2 20시, U1 착수 지시 자체가 ui_plan.md 4절 재해석 승인 — "쉼표·재개 순간의 부드러운 전환"·"깜빡이 방향과 같은 쪽에서 들어오는 쉼표 애니메이션" 명시) 대응: ui_plan.md 우선순위 1~3(전부 D 화면)을 구현 — 4번(P 화면 Spacer 정리)·P1 진행 링은 범위 밖으로 남김.
- `app/blinkercomma/session/ReaderSession.kt`에 `holdSide: StateFlow<TurnSignal>` 추가 — `showGlyph`가 ⏸를 켜는 바로 그 순간의 `transition.state.lever`를 latch(레버가 ON일 때만 Holding 진입이 가능해 이 시점의 lever는 항상 LEFT/RIGHT). `:engine`은 건드리지 않음(Effect.ShowHoldGlyph 그대로).
- `ui/ScreenUiState.kt`에 `HoldSide { LEFT, RIGHT }` 신규, `Drive`에 `holdSide` 필드 추가. `ui/ScreenInputs.kt`가 `TurnSignal → HoldSide`로 변환(OFF는 RIGHT 기본값 — ⏸가 꺼진 동안엔 화면에 안 쓰임). `CollectScreenUiState.kt`가 `session.holdSide` 수집해 배선.
- `ui/theme/HoldGlyphTransition.kt`(신규) — ⏸의 1회성 전환을 감싸는 Composable. 등장 260ms(fadeIn + 레버 방향 쪽에서 `slideInHorizontally`, 오프셋 `CommaSize.holdGlyphEnterOffset` 120dp) · 퇴장 180ms(fadeOut만, 이동 없음). `androidx.compose.animation.*` import는 detekt 규칙상 `ui/theme/`에서만 허용 — 이 파일이 그 유일한 지점.
- `ui/component/CommaWatermark.kt`(신규) — D 화면이 쉼표가 아닐 때(`!isHoldGlyphOn`)만 보이는 정적 쉼표 심볼(`ic_comma_mark` 재사용, 560dp, `CommaColors.signalDim` = SIGNAL 12% 알파). 장식이라 `contentDescription = null`, 대비 하한(`contrastPairs`/`indicatorPairs`) 대상에서 의도적으로 제외(ui_plan.md 7절).
- `ui/drive/DriveScreen.kt`를 둘을 조립하도록 교체: `!isHoldGlyphOn`이면 워터마크, `HoldGlyphTransition(visible, enterFromLeft = holdSide == LEFT) { HoldGlyph() }`를 항상 배치해 Compose가 enter/exit을 관리.
- 테스트: `ScreenInputsTest`에 레버 LEFT/RIGHT별 `holdSide` 매핑 테스트 2개 추가, 기존 쉼표 테스트에 `holdSide` 반영. `CommaThemeFloorTest`에 "⏸ 진입 애니메이션은 카메라 자리를 넘지 않는다" 경계 테스트 추가(앱 창 폭 1682dp는 D3 실측값을 테스트 상수로 — 코드에 토큰 없음).
- 문서: ARCHITECTURE.md(용어 사전 2줄·패키지 지도·8절 화면 부품), CONVENTIONS.md(공통 부품·테스트 대장), ui_plan.md 8절에 ✓ + 다음 할 일 갱신.
- 커밋 2개(`feat:` 코드, `docs:` 문서) — be7fd05 · 01b104a.

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window`, 디버그 APK, 실제 낭독 팩 — `setup/build_shots/U1_*.png` 8장, 저장소 밖)
- `scripts/check.sh .` → `tests OK (app 110, engine 119)` · `check OK`(코드 변경 직후, 캡처 전).
- 캡처로 확인: P 대기(`U1_03`)·P 낭독(`U1_04`) 그대로(이번 범위 밖, 회귀 없음) · D 읽는 중 워터마크(`U1_05`, 옅은 쉼표 심볼이 카메라 자리 밖 오른쪽에 떠 있음, 잘림·겹침 없음) · D 쉼표 RIGHT(`U1_06`)·LEFT(`U1_07`) — **정착 위치·크기 완전히 동일**(발견 1 그대로 유지, 설계대로 — 궤적만 다르고 최종 모양은 같다) · D 재개 직후(`U1_08`, 핸들 키로 강제 재개 — ⏸ 꺼지고 워터마크 재등장, 카메라는 레버가 아직 물리적으로 ON이라 시스템이 계속 띄움, 앱 동작과 무관) · 속도 신호 확인 안 됨(`U1_09`, `pm revoke --user 10`로 재현 — 앱은 `--user 10`에서 실행되므로 `--user 10`을 꼭 줘야 함, 처음 `--user 0`으로 revoke했을 땐 반영 안 됨 — 끝나고 `pm grant --user 10`으로 원복).
- 권한 안내·시스템 권한 다이얼로그·런처 아이콘은 이번 단계에서 코드를 안 건드려 재캡처 생략(U0 캡처 재사용 — D3→U0 때와 같은 판단).
- 야간 테마는 여전히 미구현(U0 5절 결론 유지, NIGHT_MODE는 SPEC 신호 목록 밖) — 캡처 대상 없음, 「확인 안 됨」으로 남김.
- 컷 1·2·3 리허설 재녹화는 생략 — 이번 변경은 `isHoldGlyphOn`이 이미 바뀐 뒤의 순수 Compose 시각 효과라 `WriteLog`·`PlayClip`·오디오 타이밍에 관여하지 않는다(엔진 레벨에서 동일 입력·동일 로그를 내는 것은 테스트 110/119 그대로 통과로 확인).

### 남은 일
- ui_plan P0-4(P 화면 `Spacer(weight=1f)` → `CommaSpace.xlarge` 고정 + 오늘 쉼표 타임라인)·P1 진행 링(5번) — 다음 라운드.
- 실차에서 측후방 카메라가 레버 방향과 무관하게 항상 왼쪽에 뜨는지는 여전히 미확인(U0부터 이어짐) — 이번 전환 방향(레버 쪽에서 진입)이 실차 카메라 위치와 시각적으로 안 겹치는지도 같이 확인 필요.
- 94eb272 블라인드 히스토리 정리 — 대표 승인 대기, 11/15 제출 전 필수(기존 숙제, 이번 단계 영향 없음). (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)

## U2 (2026-11-02 20:46)

### 한 일
- 대표 지시(11/2 20시 연장) 「허전한 부분은 ui ux 최대한 개선해서 이쁘게 해」 대응 2라운드 — U1이 남긴 `ui_plan.md` P0-4(P 화면 Spacer 정리·오늘 쉼표 타임라인)·P1(진행 링)을 구현 + 전환 애니메이션을 명세대로 다듬었다.
- `ProgressRing`(신규, `ui/component/`): P 화면 재생 버튼 둘레 정적 세그먼트 링(`Canvas.drawArc`) — 지금 문장/전체 문장만큼 채움, Tick마다 안 다시 그리고 Compose가 `sentenceNumber`/`sentenceCount` 바뀔 때만 재구성(설계 그대로 — 연속 회전 없음).
- `TodayTimeline`(신규, `ui/park/`): 오늘 쉼표 횟수만큼 점 — `TallyNumber` 숫자의 보조 그래픽. 0이면 아무것도 안 그림(원칙 4), 같은 값이 숫자로 이미 있어 접근성 트리에선 뺌(중복 안내 방지).
- `ParkScreen` 왼쪽 컬럼·`PlaybackPanel`의 `Spacer(Modifier.weight(1f))` 전부 제거 → `Arrangement.spacedBy(간격, Alignment.CenterVertically)`로 교체 — 콘텐츠 묶음이 패널 높이에 맞춰 가운데로 모여 U0가 지적한 "섹션 간 1/3 화면 높이 빈 공간"이 사라짐. `PlaybackPanel`은 묶음별 private 함수(`PackTitle`·`PlayButtonWithRing`·`PhaseStatus`·`SignalFooter`)로 쪼개 함수 15줄 상한(LongMethod threshold 16)을 지켰다 — 로직 변경 없이 분리만.
- 전환 다듬기(이번 단계에서 새로 발견): U1의 `HoldGlyphTransition` 퇴장이 `tween()` 기본 이징(FastOutSlowIn)을 썼는데 ui_plan.md 4절 명세는 퇴장을 FastOutLinearIn으로 못박아 뒀었다 — 등장 `FastOutSlowInEasing`·퇴장 `FastOutLinearInEasing`을 명시해 고침. D 워터마크도 `if`로 즉시 나타나고 사라져 ⏸ 전환과 따로 놀았는데(하드 컷), `WatermarkFadeTransition`(신규, 같은 파일)을 추가해 같은 260ms/180ms로 ⏸와 교차 페이드하게 했다.
- 신규 토큰: `CommaColors.ringTrack`(22% 알파, `signalDim`과 같은 근거로 대비 하한 제외) · `CommaSize.progressRingSize`(280dp)·`progressRingStroke`(14dp)·`timelineDot`(20dp).
- `docs/ui_plan.md` 9절 신규(이번 구현 요약) · `docs/ARCHITECTURE.md`(패키지 지도·용어 사전·8절 부품 표) · `CONVENTIONS.md`(공통 부품·캡처 집계) 갱신.
- 범위는 `ui/`(앱 레이어) 안에만 머물렀다 — `:engine`·신호 어댑터·대본(`demo/script`) 불변, 시연 3컷 시각(ⓐ~ⓓ)과 무관(U1과 같은 이유 — 화면 전환은 로그·오디오 타이밍에 관여 안 함).

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window`, 디버그 APK, 실제 낭독 팩 — `setup/build_shots/U2_01~07_*.png` 7장, 저장소 밖)
- `scripts/check.sh .` → `tests OK (app 110, engine 119)` · `check OK`(코드 변경 직후, 캡처 전) — 커밋 a410d8c.
- **VC 신호 조작 전면 무응답 발견·해결**: 이번 단계 시작 시 `curl PUT .../vehicle/gear` 등이 전부 `{"error":"emulator_inactive"}`를 반환(기존에 알려진 "속도 PUT 산발적 무반응"과 달리 기어까지 포함한 전면 무응답). 원인은 `setup/T_results_1025.md` 5행에 이미 적혀 있던 「`/browser/session` 1초 심장박동 필요」 — 이번 세션엔 그 하트비트를 보내는 프로세스가 없어 VC가 "브라우저 없음"으로 판단해 쓰기를 막고 있었다. `demo/conductor.py`/`tools/measure_signals.py`의 `keep_vc_active`와 같은 방식으로 `POST /browser/session` 1초 간격 백그라운드 루프를 띄우자 즉시 해결(이후 기어·속도·레버 PUT 전부 정상). 작업 끝에 그 백그라운드 프로세스는 종료 확인(`ps` 재확인). 코드 영향 없음, 다음 라운드를 위한 기록.
- 캡처 7장 전부 눈으로 확인 — 잘림·겹침·고아 단어 0:
  - P 대기(`U2_01`) · P 낭독(`U2_02`): 진행 링이 1/38에서 아주 얇은 호 하나만 밝게(의도대로 거의 안 보임), 왼쪽 컬럼이 가운데로 모여 예전 1/3 공백이 사라짐, 오늘 쉼표 점(타임라인)이 큰 숫자 바로 위에 붙어 보임.
  - D 읽는 중 워터마크(`U2_03`): 변화 없음(회귀 없음, U1과 동일 위치).
  - D 쉼표 RIGHT(`U2_04`)·LEFT(`U2_06`): ⏸ 정착 위치·크기 동일(U0 발견 1 유지), 카메라와 안 겹침.
  - D 재개 직후(`U2_05`, 핸들 키 시연 주입으로 재개): ⏸ 꺼지고 워터마크로 복귀, 끊기지 않고 자연스러움(육안).
  - 속도 신호 확인 안 됨(`U2_07`, `pm revoke --user 10` 재현): **신규 관찰** — 포그라운드 앱의 런타임 권한을 `pm revoke`하면 시스템이 그 앱 프로세스를 강제 종료해 다른 화면(`ai.umos.drivingview`)으로 튕겨 나갔다(U1 때는 기록에 없던 동작 — 이번에 처음 명시적으로 확인). 앱을 다시 띄우고 3초 유예(D4 수정) 지난 뒤 「○ 속도 신호 확인 안 됨」 줄이 제자리(왼쪽 정렬, `SignalFooter`의 `fillMaxWidth()`로 패널 가운데 쏠림 없이)에 뜨는 것 확인. 끝나고 `pm grant --user 10`으로 원복.
  - 권한 안내·시스템 다이얼로그·런처 아이콘·야간 테마: 이번 단계도 코드 불변(권한 안내)·미구현(야간, U0 5절 결론 유지)이라 재캡처 생략, 「확인 안 됨」으로 남김.
- `TodayTally`가 이번 단계 중 실제 상호작용으로 7→9쉼표·58→84문장까지 늘어 `TodayTimeline` 점이 누적되는 걸 그대로 캡처로 확인(지어낸 숫자 아님, 세션 내 실제 조작 결과).

### 남은 일
- 진행 링을 바라보며 "38개 세그먼트가 실제 낭독 팩(38문장)보다 더 긴 팩으로 바뀌면 촘촘해 보이는지"는 다음 팩 교체 시 재확인.
- 실차 카메라-레버 방향 무관 여부(U0부터 이어짐) — 여전히 미확인.
- VC `/browser/session` 하트비트가 필요하다는 사실은 `T_results_1025.md`에 이미 있었지만 `tools/device.py`(수동 조작용 — `tap_text` 등)엔 하트비트 로직이 없다 — 다음에 VC로 수동 조작할 일이 있으면 먼저 하트비트부터 띄울 것(이번처럼 매번 원인 재탐색하지 않도록, 코드 변경은 이번 범위 밖으로 남김).
- 94eb272 블라인드 히스토리 정리 — 대표 승인 대기, 11/15 제출 전 필수(기존 숙제, 이번 단계 영향 없음). (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)

## UJ (2026-11-02 20:56)

### 한 일
- UI 블라인드 심사 — **이번 단계는 심사·기록만, 코드 수정 0**.
- `demo/rehearsal.py`로 시연 대본 3컷(1회, 디버그 APK, 에뮬레이터 `-no-audio -no-window`) 녹화 → 79.5초 mp4 + logcat. 핵심 전환 시각 9곳을 `ffmpeg`로 프레임 추출(저장소 밖 스크래치패드) — 로그 시각 기준 추정 offset이라 ffmpeg 키프레임 스냅으로 ±수백ms~1초 어긋날 수 있음(문서에 한계로 명시).
- 외부 블라인드 심사위원 3명(현대차 ccNC/Pleos UX 디자이너 · 차량 HMI 안전 전문가 · 앱마켓 큐레이터, 전부 Agent 도구로 독립 실행 — 서로 점수·코멘트 모름, 이 세션 맥락도 모름)에게 U2 캡처 7장 + 녹화 프레임 9장 + SPEC 「한 줄」 소개만 전달(사내 문서·점수 비공개). 각자 100점(시각 완성도30·운전 중 안전25·한눈 이해25·시연 인상20) + 반드시 고칠 것 5개 받음.
- `docs/ui_judge.md` 신규 — 3인 점수표·공통 지적 5개(겹치는 심사위원 수 순)·단독 지적·한계 정리.

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window`, 디버그 APK, 실제 낭독 팩)
- 리허설 1회 성공(새 설치→권한→컷1·2·3), `rehearsal.py`가 내부적으로 VC 하트비트(`keep_vc_active`)를 띄워 U2가 남긴 「다음에 VC 쓸 때 하트비트부터」 숙제를 그대로 해소 — 수동 조치 불필요했음.
- 심사 점수: UX 디자이너 74/100 · HMI 안전 전문가 76/100 · 앱마켓 큐레이터 76/100 · 평균 75.3/100. 근거 줄은 `docs/ui_judge.md`.
- 공통 지적 1순위(3명 전원): 주행(D) 화면 쉼표 워터마크가 너무 옅거나(U2_03·U2_05) 우측 끝에서 잘려 보이고(frame_23/27/71/76), 단독으로 보면 「빈 화면」으로 읽힌다.
- 2순위(2명): 깜빡이 ON 때 시스템 블라인드스팟 카메라 영상과 앱 ⏸ 표시가 겹쳐 보여(U2_04·U2_06) 시선을 더 끌 수 있다는 지적 — 원칙 11(카메라 자리 밖 배치)은 지켰지만 카메라 바로 옆이라 「겹쳐 보인다」는 인상 자체는 남음.
- 3순위(2명): 쉼표 중 상태 표시(⏸ 하나)가 작아 흘끗 보기 식별성이 낮다는 지적 — M8 upgrade_plan 4-2(D 화면 「쉼표 N」 배지)와 같은 방향.
- 4순위(2명): 「속도 신호 확인 안 됨」(U2_07) 예외 상태 UI 톤이 메인 디자인과 어긋난다는 지적.
- 5순위(1명, 치명적 등급): 캡처 16장 전부에 앱 로고·이름이 없어 스토어 입점 심사 관점에서 식별 불가.

### 남은 일
- 공통 지적 1~5순위 적용 여부·우선순위는 대표 승인 필요 — 특히 2·3순위는 M8/upgrade_plan.md 4-1·4-2(Mia 설계 → UI 개선 실행기)와 겹치므로 합쳐서 검토 권장.
- 94eb272 블라인드 히스토리 정리 — 대표 승인 대기, 11/15 제출 전 필수(기존 숙제, 이번 단계 영향 없음). (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)
- 42dot 문의 3건(M8 upgrade_plan 2-3) — 대표 승인 대기, 이번 단계 영향 없음.

## U3 (2026-11-02 21:15)

### 한 일
- 대표 지시(11/2 20시 연장) 「허전한 부분은 ui ux 최대한 개선해서 이쁘게 해」 + 11/2 21시 「최우수·대상, 공식 기준으로」 대응 — UJ 블라인드 심사 공통 지적 5개(`docs/ui_judge.md`)와 `ideas/v3/meeting/M8/upgrade_plan.md` 4절(차량특화성)의 Mia 담당 항목 2개(4-1·4-2)를 합쳐서 반영했다(M8 decision.json `keep: true` 확인 후 착수). 상세 설계·근거는 `docs/ui_plan.md` 10절.
- **M8 4-1**: `strings.xml`의 `auto_resume_on_detail`·`auto_resume_off_detail`을 M8이 정한 문구로 교체(핸들 키 폴백 경로 명시).
- **M8 4-2 + UJ 3순위**: `HoldCountBadge`(신규, `ui/component/`) — D 화면 ⏸ 옆 「쉼표 N」, P 화면과 같은 호박색. `HoldCountBadgeTransition`(500ms 페이드, `ui/theme/HoldGlyphTransition.kt`)을 추가해 ⏸(260ms)보다 한 박 늦게 들어오게 했다. ⏸의 SPEC 고정 좌표(1197~1263dp)를 건드리지 않으려고 배지를 ⏸와 묶어 슬라이드시키지 않고 콘텐츠 박스 `CenterEnd`에 독립 배치. `ScreenUiState.Drive`에 `todayCommas` 필드 추가(`ScreenInputs.toUiState`가 `reader.tally.commas` 그대로 넘김 — 지어낸 숫자 아님).
- **UJ 1순위(워터마크 너무 옅음·우측 끝 잘림)**: `CommaColors.WATERMARK_ALPHA` 12%→20%. DriveScreen 콘텐츠 박스에 `end = CommaSpace.large`(32dp) 패딩을 더해 중심점을 왼쪽으로 당김(⏸ 좌표는 1215dp로 SPEC 범위 안 유지) — 재실측으로 잘림 0 확인.
- **UJ 2순위(카메라·⏸가 겹쳐 보인다는 인상)**: `CameraBoundaryLine`(신규, DriveScreen.kt 내부 private) — 카메라 폭 경계(780dp)에 아주 옅은 정적 세로선(기존 토큰 `CommaColors.ringTrack`·`CommaSize.panelBorder` 재사용, 새 토큰 0개). 카메라·⏸ 좌표는 그대로.
- **UJ 4순위(「확인 안 됨」 톤 어긋남)**: `StatusLine`에 `DemoInjectedTag`와 같은 테두리 칩 스타일 추가. 경고색은 여전히 안 씀(신호 없이도 동작하므로).
- **UJ 5순위(브랜드 식별 요소 없음) — 코드 반영 안 함**: D 화면에 로고·글자를 넣으려면 원칙 1 재해석 범위를 더 넓혀야 해 별도 승인 사이클이 필요하고 11/8 동결까지 여유가 없다. `docs/ui_judge.md`·`ui_plan.md`에 「스토어 캡처는 로고가 있는 P 화면을 포함해서 고른다」는 운영적 해결로 기록.
- 테스트: `ScreenInputsTest`에 Drive의 `todayCommas` 전달 테스트 1개 추가(app 110→112). 문서: `ARCHITECTURE.md`(8절 부품·패키지 지도·용어 사전) · `CONVENTIONS.md`(공통 부품·테스트 대장) · `ui_plan.md` 10절(신규) · `ui_judge.md`(반영 결과 절 추가).
- 범위는 `ui/`(앱 레이어) 안 — `:engine`·신호 어댑터·대본 불변, 시연 3컷 시각과 무관(U1·U2와 같은 이유).

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window`, 디버그 APK, 실제 낭독 팩 — `setup/build_shots/U3_01~08_*.png` 8장, 저장소 밖)
- `scripts/check.sh .` → `tests OK (app 112, engine 119)` · `check OK`(코드 변경 직후·문서 추가 후 재확인 2회).
- 캡처 8장 전부 눈으로 확인 — 잘림·겹침·고아 단어 0:
  - 권한 안내(`U3_01`, 새 설치 직후 우리 앱 화면만 — 시스템 다이얼로그는 아직 안 뜬 상태) · P 대기(`U3_02`) · P 낭독(`U3_03`): 새 자동 재개 문구("레버가 돌아오지 않아도 핸들 재생 버튼으로...") 잘림·겹침 없이 정상 렌더.
  - D 읽는 중 워터마크(`U3_04`): 20%로 또렷해짐, 화면 우측 끝까지 여유(잘림 0) 재확인 — 이전 12%·클리핑 지적(UJ 1순위) 해소.
  - D 쉼표 RIGHT(`U3_05`, 「쉼표 1」)·LEFT(`U3_06`, 「쉼표 2」): ⏸ 정착 위치·크기 RIGHT/LEFT 동일(U0 발견 1 그대로 유지), 배지가 카메라와 충분히 떨어진 자리에 또렷하게 표시, 숫자는 실제 조작 결과(지어낸 값 아님).
  - D 재개 직후(`U3_07`): ⏸·배지 꺼지고 워터마크 복귀, 하드 컷 없이 자연스러움(육안).
  - 속도 신호 확인 안 됨(`U3_08`, `pm revoke --user 10` 재현): 테두리 칩 스타일로 바뀐 「○ 속도 신호 확인 안 됨」 확인, 오늘 카운트 13문장·2쉼표(이번 단계 실제 조작 결과) + `TodayTimeline` 점 2개 정상.
- VC 신호 조작은 이번에도 `/browser/session` 하트비트 필요(U2 숙제 그대로, `rehearsal.py` 안 쓰고 직접 조작할 때는 매번 필요) — 내가 만든 캡처 스크립트에 하트비트 스레드 포함해 처리, 작업 끝에 종료 확인.
- 권한 다이얼로그 자동 확인 함정: 첫 시도에서 `adb shell input keyevent ENTER`로 시스템 권한 다이얼로그를 닫으려 했으나 반응 없었다(화면에 "허용" 텍스트가 우리 앱 버튼과 시스템 다이얼로그 두 곳에 동시에 있어 `tap_text`가 모호했던 게 원인으로 보임) — `uiautomator dump`로 정확한 bounds를 찾아 좌표 탭으로 바꿔 해결. 이 함정은 향후 재현용으로만 기록(코드 영향 없음).

### 남은 일
- UJ 공통 지적 5순위는 코드 미반영(운영적 해결로 기록, 위 참고) — 다음에 D 화면 글자 허용 범위를 넓히는 논의가 생기면 재검토.
- 94eb272 블라인드 히스토리 정리 — 대표 승인 대기, 11/15 제출 전 필수(기존 숙제, 이번 단계 영향 없음). (**2026-11-02 M3 갱신**: 히스토리 재작성으로 해시 변경, 블라인드 위반 해소(11/2) — 전체 커밋·reflog·unreachable object grep 0건 재확인, 상세는 docs/qa_M3.md)
- M8 upgrade_plan의 나머지 항목(1-1·1-2·2-1·2-2·2-3·3·5)은 Mia 담당 밖(James·Mason·Ryan) — 이번 단계 영향 없음.
- 실차 카메라-레버 방향 무관 여부(U0부터 이어짐) — 여전히 미확인.

## M3 (2026-11-02 21:40)

### 한 일
- 회귀 검수 담당(Mason): U0~U3(UI/UX 개선)·UJ(블라인드 심사) 반영 결과 전체 회귀. 앱 지우고 새로 설치(`assembleDebug` 재빌드) → `demo/rehearsal.py` 2회 → 원칙(주행 중 조작 0·주행 중 새 글 최소·제어 0·실명 0·시연 주입 표시) 실측 → 8상태 수동 캡처 대조(잘림·겹침) → `scripts/check.sh` → 실명 grep(코드·**전체 git 히스토리** `git log --all -p`·리소스). 코드 수정 0, 결과는 `docs/qa_M3.md`.
- **새 결함 발견(결함 1, 상)**: 11/2 21:10 대표 지시(D6)·M8 decision.json must_fix 목록의 「자동 재개 기본값 「끔」 전환」이 코드에 반영되지 않았다 — `ReaderSettings.kt:11`이 여전히 `getBoolean(AUTO_RESUME, true)`. 같은 지시의 나머지 두 조각(P 화면 보조 문구 1줄·D 화면 「쉼표 N」 배지)은 U3에서 정확히 반영됐는데 이 조각만 빠졌다. `demo/script/cut3_key_resume.tsv`가 매번 수동으로 토글을 끄고 시작해 리허설로는 안 걸렸다. 코드는 고치지 않음 — 재현만 보고.
- 블라인드 히스토리 재확인: `scripts/check.sh`의 블라인드 검사는 작업 트리만 보고 과거 커밋 diff는 안 본다는 걸 확인하고, 27개 커밋 전부 `git show`로 개별 grep + `git log --all -p` 전체 grep + `git fsck --unreachable` + `git reflog --all` + `git cat-file -e 94eb272`를 직접 돌려 **94eb272 블라인드 위반이 11/2 20시 히스토리 재작성으로 실제로 해소됐음**을 재확인(해당 해시 자체가 더 이상 존재하지 않음). progress.md·qa_M2.md의 옛 해시 언급 전체(9+3곳)에 갱신 메모를 추가했다(문서만, 히스토리 자체는 건드리지 않음).

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window`, 디버그 APK, 실제 낭독 팩 — `setup/build_shots/M3_01~11_*.png`, 저장소 밖)
- `scripts/check.sh .` → `tests OK (app 112, engine 119)` · `check OK`(U3 이후 변경 없음, 재확인만).
- 리허설 2회 전부 컷 1·2·3 성공, `rehearsal_summary.py` 교차비교 「같음(차이 ≤0.15s)」. qa_M2 결함 4(컷 2 VC drift)는 이번 2회에선 재발 0(최대 차이 0.05s) — 결함 자체가 닫힌 건 아니고 간헐 재현 특성 그대로.
- 8상태 캡처(권한 안내·P 대기·P 낭독·D 워터마크·D 쉼표 RIGHT/LEFT·D 재개 직후·속도 확인 안 됨·자동 재개 끔) 전부 잘림·겹침 0, UJ 공통 지적 1~4순위 반영 결과(워터마크 20%·클리핑 0·카메라-⏸/배지 비겹침·「쉼표 N」 배지·예외 상태 칩 톤) 육안 확인, RIGHT/LEFT 정착 위치·크기 완전 동일(U0 대칭 유지).
- D 화면 `uiautomator dump`(기어 D·속도 40·쉼표 중) → `app.blinkercomma` 노드 1개, `clickable="true"` 0개, text·content-desc 전부 빈 문자열 — 원칙 1 유지 재확인.
- 실명 grep 전체 git 히스토리(27개 커밋) 0건, 작업 트리 0건(위 「한 일」 참고).

### 남은 일
- 결함 1(자동 재개 기본값 「끔」 미전환) 수정 — 대표 확인 필요. 수정 시 11/10 촬영 대본(컷 1·2가 기본값에 의존)도 같이 점검.
- 그 외는 docs/qa_M3.md 「안 한 것/범위 밖」과 동일(런처 아이콘·카메라 경계선 픽셀 검증·실차 카메라-레버 방향·42dot 문의 — 이번 단계 영향 없음).

## D6 (2026-11-02 21:42)

### 한 일
- **qa_M3.md 결함 1 수정(유일한 결함)**: `app/.../data/ReaderSettings.kt` `prefs.getBoolean(AUTO_RESUME, true)` → `false`로 변경 — 새 설치 기본값 「자동 재개 끔」(M8 decision.json must_fix, 코드 추가 없이 설정값 전환만, 지시대로). `engine/.../CommaCardParams.kt`의 `isAutoResume = true`(엔진 자체 기본값, 앱은 `AppContainer.kt`에서 항상 `ReaderSettings` 값으로 override — 실제로 안 쓰임)는 그대로 둠. commit-msg 훅(`fix:`은 테스트 동반 필수)에 맞춰 기본값을 `ReaderSettings.DEFAULT_AUTO_RESUME` 상수로 뽑고 `ReaderSettingsTest`(테스트 1개 × debug·release 2변형 = app 112→114, D1 관례와 동일) 추가 — `ReaderSettings` 본체는 Context가 있어야 생성돼 Robolectric 없인 단위 테스트가 안 되므로(기존에도 없었음) 상수만 고정, 실제 동작은 아래 에뮬레이터 실측으로 확인(`CommaCardParamsTest`와 같은 「기본값 고정 테스트」 패턴).
- 기본값이 꺼지면서 영향받는 두 시연 컷(「레버가 돌아오면 다음 문장」 = R5 자동 재개 흐름)에 숨은 조작 추가: `demo/script/cut1_breath.tsv`·`cut2_wait_go.tsv`에 각 `0 app auto_resume ON`(준비 줄, `play` 전에 배치해 D4가 고쳤던 "▶ 뒤 준비 줄 시간 압박" 구간과 안 겹침) — conductor의 `play_app`이 이미 화면 상태를 확인하고 필요할 때만 탭해(상태 확인 후 탭, D1부터 있던 로직) 중복 탭 없음. `cut3_key_resume.tsv`(핸들 키로만 재개하는 컷)는 이미 자체적으로 「자동 재개 끔」을 명시적으로 탭하므로 수정 불필요(기본값이 끔이 된 지금은 사실상 같은 상태라 탭이 생략됨 — idempotent).
- `docs/SPEC.md` R8 행·컷 1·2·3 「준비」 절을 새 기본값·새 숨은 조작에 맞게 갱신. `docs/qa_M3.md` 결함 1 절에 해결 메모 추가(히스토리는 그대로 두고 갱신 메모만 — M3가 94eb272에 했던 방식과 동일).
- release APK 재빌드(`:app:assembleRelease`, 키스토어 `~/.blinker-comma/`) → `apk/CommaLane_깜빡이쉼표_release.apk` 교체. `docs/status_for_form.md` 갱신(R5·R8 행, 시연 대본·릴리스 APK 행 — D6 재실측 근거로).
- `setup/build_shots/rehearsal_final_v2.mp4`(성공 2회차 원본 그대로, 저장소 밖) — 2026-11-02 M2·M3와 같은 관례(편집 없이 원본).

### 실측 (에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window`, 근거 `docs/logs/d6_*.txt`)
- `scripts/check.sh .`(코드 변경 직후·문서 갱신 뒤·테스트 추가 뒤 총 3회) → `tests OK (app 114, engine 119)` · `check OK`. 커밋 시도 1차는 commit-msg 훅(「fix: 커밋인데 테스트 변경이 없음」)에 막혀 `ReaderSettingsTest` 추가 뒤 재시도, 블라인드 검사도 1차 걸림(`docs/progress.md` 초안에 로컬 절대경로가 그대로 남아 있었음 — 즉시 지우고 재확인).
- release APK: `apksigner verify --verbose`(Android SDK build-tools 34.0.0) → v2 스킴 통과(`Verifies` · v1/v3/v3.1/v4 안 씀, 기존과 동일 설정).
- **새 설치 기본값 직접 확인**: `adb uninstall` → `adb install` → 권한 허용 2단계 → P 화면 캡처 `setup/build_shots/D6_01_park_default_autoresume_off.png` — 「자동 재개: 끔」 토글이 꺼진 채로 기본 표시됨(결함 1 해소 확인), 잘림·겹침 0.
- 시연 대본 리허설(디버그 APK) **2회 연속 성공**(새 설치 포함, `demo/rehearsal.py`) — `docs/logs/d6_rehearsal_01_logcat.txt`·`d6_rehearsal_02_logcat.txt`, 녹화 `setup/build_shots/d6_rehearsal_01.mp4`(86.9초)·`d6_rehearsal_02.mp4`(86.9초, 180초 상한 안).
  - 컷 1: `Comma: OFF held 0.80s → play s=3`(1회차)·`0.80s → play s=4`(2회차 표기는 컷 2) — 레버로 재개가 **실제로** 작동함(기본값이 꺼진 상태에서 대본이 탭으로 켰다는 뜻).
  - 컷 2: `Comma: OFF held 0.83s → play s=4`(1회차)·`0.80s → play s=4`(2회차) — 같은 흐름 재확인.
  - 컷 3: `Comma: autoResume=off → keep hold`(두 회차 모두) — 기본값 그대로(끔) 유지, 핸들 키로만 재개(`mediaKey PLAY → play`) 정상.
  - `demo/rehearsal_summary.py` 교차비교 → 컷 1·3 「같음」, **컷 2만 "줄 순서·글자가 다름"** — 실제 내용은 같고 `OFF held 0.83s` vs `0.80s`처럼 텍스트에 박힌 디바운스 측정값이 달라 비교 도구가 다르다고 판정한 것(두 회차 다른 줄의 시각차는 전부 ≤0.15s, `moving → comma armed`만 0.13s 차이로 허용치 안). qa_M2 결함4(컷 2 VC 신호 산발적 drift)와 같은 계열 — 이번 변경(auto_resume 숨은 조작 추가)이 새로 만든 문제가 아님, 기존에 열려 있던 간헐 결함 그대로.
- 확인 안 됨: 결함 1 수정에 대한 단위 테스트(Robolectric 미도입, 엔진 레벨엔 이 로직이 없어 테스트 불가 — 위 실측으로 대체) · 42dot 문의·실차 카메라-레버 방향(기존 숙제, 이번 단계 영향 없음).

### 남은 일
- qa_M2 결함4(컷 2 VC 신호 산발적 drift) — 여전히 열려 있음(간헐 재현, 근본 원인 미확정). 11/10 실촬영에서 날 수 있다는 전제 그대로.
- 94eb272 블라인드 히스토리 — M3에서 해소 확인됨(이번 단계 영향 없음, 재확인 안 함).
- 42dot 문의 3건 — 대표 승인 대기, 이번 단계 영향 없음.
- 제출 패키지(P1 산출물)는 D5 기준으로 만들어졌으므로 release APK·접수양식 재배포본 반영은 P2 담당 범위(이번 D6은 저장소 안 구현·검증만).

## P2 (2026-11-02 22:05)

### 한 일
- 제출 패키지 v2(`submit/blinker-comma_제출본/`, 저장소 밖) — 이 저장소(`blinker-comma/`)는 읽기만 했다(APK·git archive 소스로만 사용, 코드 변경 0).
- M8 `decision.json`(keep: true, 79.0 1위) 확인 후 `upgrade_plan.md`의 James·Mason 담당 항목(기획서 문구)을 `submit/build_form_blinker-comma.py`에 반영: ① S3 ④번·「함께 적는 한계」·S5 약점란 — 자동 재개 **기본값 끔** 문구로 통일(D6 코드 변경과 일치), 방향지시등 레버/램프 기준 모호성 + R2 실측 안전성 문장 추가 ② S4 데모 시나리오 4·6번 — 기본값 끔을 반영해 「자동 재개 켬으로 설정해 두었다면」 / 「기본값인 끔 상태라면」으로 조건문 전환 ③ S6 사업성 2·3번 — 현대차그룹 쪽은 Pleos 미디어 정책팀 애드온 제안 문구로, 오디오 구독사 쪽은 윌라 Pleos 앱마켓 미입점 구조 설명으로 교체 ④ MAU 설치율 3/4·30일 잔존 1/3에 출처 각주 ⁸ 신설(`ideas/v3/meeting/M4-2/panel.md` 인용, 숫자 458은 그대로).
- **upgrade_plan.md 원문과 다르게 처리한 것(대표 11/2 21:10 지시 — 지어내기 금지)**: 「윌라 입점 관심·파트너십 의향 접촉을 진행 중입니다」·「42dot에 질의 중이며」 문구는 그대로 쓰지 않았다. 윌라 관심 메일·42dot 질의 둘 다 실제로는 발송 0건(대표 승인 대기, `docs/progress.md` 여러 단계에 carry-over로 기록된 그대로)이라, 「메일/문의 문안은 작성을 마쳤으나 아직 발송 전」으로 정직하게 고쳐 썼다.
- 시연 영상 v2: D6 최종 리허설 원본(`setup/build_shots/rehearsal_final_v2.mp4` = `d6_rehearsal_02.mp4`, 86.94초 — 자동 재개 기본값 끔 반영 리허설)에 v1과 같은 방식(타이틀 카드 1회 고지 + 엔드 카드, concat)으로 다시 포장했다. 카드 디자인은 v1 프레임을 ffmpeg로 뽑아 배경색(#0A0A0C)·강조색(#F0A13A) 등을 그대로 추출해 Playwright(HTML→2560×1440 PNG)로 재현 — 로고·실명 없음. v1 제출본에 있던 컷 재배치·구간별 자막 구분(M8 Ryan 안)은 이번엔 넣지 않았다(대표 11/2 20시 마지막 지시 「타이틀·엔드 카드 방식 그대로」를 문자 그대로 따름, CHECKLIST.md에 남겨 둠).
- **버그 발견**: v1 제출본의 APK가 D6 이전(P1 시점) 구버전이었다 — 자동 재개 기본값이 아직 「켬」인 빌드. `blinker-comma/apk/`의 D6 재빌드본으로 교체, 구버전은 `old/CommaLane_깜빡이쉼표_release_v1.apk`로 보관.
- 소스 zip `git archive HEAD`로 재생성(D6 커밋 9171f7f까지 포함 — v1 zip은 D6 이전 시점).
- GitHub push 안전성 재확인(M3 결과를 다시 믿지 않고 직접 재실행): `git cat-file -e 94eb272` → 객체 없음(정상) · `git log --all -p` 실명 grep 0건 · `git fsck --unreachable`로 나온 unreachable object 10개(트리 9·블롭 1) 전부 `git cat-file -p`로 내용 확인 — 블라인드 위반 0건 · `git push`는 reachable object만 전송하므로 이 unreachable object들은 애초에 push 대상도 아님. CHECKLIST.md에 "이 저장소를 그대로 push해도 됩니다"로 명시.
- 02 블라인드 재검증: v2 docx를 02 섹션(테이블 인덱스 31~45)만 분리 추출해 성명·연락처·이메일·로컬 경로·과장 문구 전수 grep 0건(전체 문서 기준으로는 01·04의 운영용 실명만 검출 — 설계대로).
- PDF 잘림 검사: v2는 추가된 문구로 7쪽→8쪽, 전체 8쪽 100dpi 캡처 육안 확인(잘림·표 침범·빈 쪽 0건).
- `CHECKLIST.md` v2로 전면 갱신 — GitHub push 항목을 "11/2 완료(재작성·전체 기록 실명 0건)"로, 이 저장소를 그대로 push해도 된다고 명시. v1 파일(접수양식·시연영상·소스zip·구버전 APK) 전부 `old/`로 이동.

### 실측
- M8 `decision.json`: `keep: true`, 외부 블라인드 3인 평균 79.0/100(2위 66.7) — 교체 아님, 유지 확인 후 착수.
- git 히스토리 재검증(위 「한 일」 참고) — `94eb272` 미존재, `git log --all -p`·unreachable object 10개 전수 실명 grep 0건.
- 영상: `ffprobe`로 raw(`rehearsal_final_v2.mp4`) 86.940156초 == `d6_rehearsal_02.mp4`(sha1 동일) 확인 → 포장 후 93.933333초(타이틀 3.5s+원본 86.94s+엔드 3.5s), 2560×1440 h264 정상 재생(시작·중간·끝 프레임 샘플 확인), strings 실명 grep 0건.
- 소스 zip `unzip -l` grep: `.git/`·`local.properties`·키스토어·`*.jks` 0건, 11.1MB.
- APK 교체 전/후 md5 다름(P1 구버전 vs D6 신버전) — 혼입 사고를 실측으로 잡아냄. 이 세션엔 `apksigner`가 없어 재서명 검증은 D6 기록(`docs/progress.md` D6, v2 스킴 통과)을 인용만 함(재검증 안 함, CHECKLIST ⚠️ 유지).
- 02 섹션(테이블 idx 31~45) 분리 추출 grep: 성명·연락처·이메일·로컬경로·과장문구 0건(7103자).
- PDF `pdftoppm -r 100`으로 8쪽 전부 캡처, 육안 확인 — 잘림·침범·빈 쪽 0건.

### 남은 일
- 대표 몫(CHECKLIST.md 2~7번 그대로): GitHub push 실행 + GITHUB_URL로 재빌드, 메일 발송 승인, 제목 줄 확인, 윌라 메일 발송, 42dot 질의 발송, 가출원 여부 결정.
- M8 Ryan 안(영상 컷 재배치·구간별 자막 구분·타임스탬프 상시 오버레이)은 이번 v2에 반영하지 않음 — 필요하면 다음 라운드.
- qa_M2 결함4(컷 2 VC 신호 산발적 drift) 여전히 열려 있음(11/10 실촬영 위험 그대로, 이번 단계 영향 없음).
- M8 upgrade_plan의 Mason 항목(2-2 외부 앱 오디오 포커스 추가 실측 — 멜론 등 사이드로드 가능 여부)은 Mia/UI 담당이 아니라 Mason/D 단계 몫, 이번 P2 범위 밖.

## P3 제출 직전 정정 (2026-11-13)

### 한 일
- P 화면 사용법 3번 줄을 자동 재개 설정에 맞춤: 끔(기본값) = 「핸들 재생 버튼으로」, 켬 = 「레버가 돌아오면」(`resumeStepTitle`, `HowItWorksTest`). README 한 줄 소개·실행 4단계도 기본값 기준으로 정정.
- 릴리스 APK 재빌드 → `apk/CommaLane_깜빡이쉼표_release.apk` 교체.
- 시연 영상은 타이틀 카드 고지 문구만 교체(「소리는 편집 합성」 삭제 → 「실제 소리는 녹음되지 않음」 + 컷 1·2 자동 재개 켬·컷 3 기본값 표기), 본편 구간은 v2와 동일.

### 실측
- `scripts/check.sh .` → `tests OK (app 118, engine 119)`, check OK.
- `apksigner verify` → v2 스킴 통과.
- 에뮬레이터(Pleos_Connect_34)에 릴리스 APK 새 설치 → P 화면 끔·켬 두 상태 캡처, 3번 줄 전환·잘림 없음 확인.

## P3-2 1등 개선 라운드 (2026-11-14)

블라인드 심사 3인(사업·차량 UX·SDK, 서브에이전트) 기준점 44·30·36/100. 공통 지적: 기능 하나 크기, 콘텐츠 3분, 사업성(가상 패널·MAU 0~601), 방어적 문서, 영상·양식 소리 표기 불일치.

### 한 일
- R11 후진(R) 쉼표 — 레버 없이 문장 끝에서 멈춤, R 구간 한 번(`ReaderReverseTest`·`Cut4ReverseTest`), 로그 `clipEnd … gear=R → hold`.
- 이어 듣기 — `PositionStore`(팩이 바뀌면 처음부터). 진행 라벨 「N번째 문장 · 전체 M」.
- 쉼표 중 「다음」 버튼이 한 문장 건너뛰던 결함 수정(redteam W-1).
- 1시간 팩 `packs/aesop` — LibriVox 14개 장 998조각 53분, `tools/build_long_pack.py`(원문 쉼표 분할). 기본 팩 전환.
- 다른 앱용: `docs/INTEGRATION.md`·`IntegrationExampleTest`·`tools/pack_from_transcript.py`(WebVTT·SRT → 팩).
- 시연: 컷 4 「후진」 대본, 리허설 2회 성공(`docs/logs/p3_rehearsal_01·02_logcat.txt`), `demo/soundtrack.py`로 로그 시각대로 소리 입힘.

### 실측
- check.sh: tests OK (app 118, engine 127). release APK apksigner v2 통과.
- 리허설 2회 비교: 4컷 줄 순서 같음, 차이는 컷 2 레버 OFF 전달 시각 0.17초 하나.
