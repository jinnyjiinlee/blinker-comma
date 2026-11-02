# 코드 기획서 — 깜빡이 쉼표 (Blinker Comma)

> 처음 보는 사람이 이 한 장으로 「어디에 무엇이 있고, 왜 그렇게 나눴는지」 알게 하는 문서.
> 기능 범위·원칙은 `docs/SPEC.md`, 코드 규칙은 `CONVENTIONS.md`.

## 1. 한 문장 구조

**깜빡이·기어·속도·플레이어·핸들 키·오디오 포커스를 이벤트 하나로 모아 → 순수 리듀서가 「멈춤/재생/⏸/로그」 효과를 내고 → 앱이 그대로 실행한다.**

```
[Pleos 깜빡이·기어 · AAOS 속도]  ─CarEvent─┐
[MediaPlayer 클립 끝]            ─ClipEnded─┤
[P 화면 ▶ · 핸들 키(MediaSession)] ─Pressed─┼─▶ reduce(ReaderState, ReaderEvent, CommaCardParams) ─▶ Effect 목록
[오디오 포커스]                   ─Focus────┤        (engine — 시계·SDK·코루틴 없음)          PlayClip · PauseClip
[앱 타이머 @ nextDeadline]        ─Tick─────┘                                               ShowHoldGlyph · WriteLog · SaveTally
```

- 시간은 이벤트의 `at`(단조 시각)만 쓴다. 디바운스·상한은 앱이 `ReaderState.nextDeadline(params)` 시각에 `Tick`을 한 번 보내 판정한다 — 100ms 폴링이 아니라서 로그 `OFF held 0.80s`가 실제 유지 시간과 같다.
- 「자동 재개」는 P 설정이라 앱이 `CommaCardParams.DEFAULT.copy(isAutoResume = …)`로 매번 넘긴다.
- 스레드: 엔진·세션·서비스 콜백은 전부 **메인 스레드 하나**(Pleos·AAOS 콜백은 Flow로 메인에 모임, 시각 `at`은 콜백 순간). MediaPlayer만 전용 스레드 둘(열기 `clip-player` · 놓기) — 에뮬레이터에서 수 초씩 막혀 메인을 막지 않게(D2 실측).
- 문장 끝은 플레이어 완료 콜백이 아니라 **팩 `durationMs` 시계**(`ClipPlayer`) — 에뮬레이터 콜백이 늦고 이르게 와서(D2).

## 2. 모듈

| 모듈 | 성격 | 의존 | 테스트 |
|---|---|---|---|
| `:engine` | 순수 Kotlin(JVM) — 쉼표 판정·낭독 상태 리듀서·로그 줄·팩 검사 | kotlinx-serialization-json(팩 JSON) | JVM 단위 + 대본 리플레이(`demo/script`), Kover 줄 90%·분기 85% |
| `:app` | Android — SDK 어댑터·서비스·플레이어·(D3) 화면. `src/debug`에 시연 주입 수신기 | `:engine`, Pleos Vehicle 2.3.2, androidx.media | 값 변환·재생 위치·주입 해석(JVM) + 에뮬레이터 실측(`tools/measure_signals.py` → `docs/signals_measured.md`) |

## 3. 용어 사전 (코드에서 이 단어만 쓴다)

| 한국어 | 코드 | 뜻 | 안 쓰는 말 |
|---|---|---|---|
| 레버 | `lever`·`TurnSignal` | 방향지시 레버(깜빡이). LEFT·RIGHT = ON(`isOn`) | blinker, indicator |
| 쉼표 | `Holding`·`hold` | 문장 끝(또는 상한 뒤 중간)에서 낭독을 멈춘 상태. 화면엔 ⏸ 하나 | pause(포커스·정지와 헷갈림) |
| 쉼표 예약 | `armedAt`·`ARMED`·`classifyArm` | 레버 ON + D + 이동 중 → 이번 문장 끝에서 멈출 준비 | trigger, schedule |
| 쉼표 표시 | `ShowHoldGlyph` | D 화면의 ⏸ 하나. 소리가 멎을 때 켜고 다시 날 때 끈다 | icon, badge |
| 문장 | `index`·`SentenceClip` | 클립 1개 = 문장 1개. 로그 번호는 1부터(`s=2` = index 1) | track, segment |
| 디바운스 | `offDebounce`·`offSince` | 쉼표 중 레버 OFF가 이만큼 유지돼야 재개(R5) | delay, grace |
| 상한 | `endWaitLimit` | 예약 뒤 문장이 이 안에 안 끝나면 중간에서 멈춤(R4) | timeout |
| 재개 | `resume`·`ResumedAfterOff` | 쉼표를 풀고 문장 처음부터 재생 | restart, unpause |
| 포커스 멈춤 | `FocusPaused` | 다른 앱이 오디오 포커스를 잠시 가져가 멈춤(R10). 돌려받으면 멈춘 자리부터 | duck |
| 정지 | `Stopped`·`stop` | 재생 안 함. 앱 실행 직후도 여기(자동 재생 금지) | idle |
| 누름 | `PlayPressed`·`PausePressed`·`SkipPressed`·`PressSource` | P 화면 버튼(`SCREEN`)·핸들 미디어 키(`MEDIA_KEY`) | click, tap |
| 오늘 카운트 | `ReadingTally` | 마친 문장 수·쉼표 수. 날짜 키는 앱이 붙임 | stats |
| 팩 | `SentencePack`·`PackFile`·`PackProblem` | APK 에셋 콘텐츠 묶음(`pack.json` + 클립) | album, book |
| 확인 안 됨 | `null`(`gear`·`speedMps`) | 신호를 못 받음 — 지어내지 않는다(원칙 4) | default, 0 |
| 대본 | `demo/script` 폴더의 TSV · `StageLine` · `anchor` | conductor·시나리오 테스트·종이 순서표가 같이 읽는 시연 순서 | scenario file |
| 시연 주입 | `DemoInjectReceiver`·`DemoInjectionMarks`·`toInjectedEvent` | VC로 못 넣는 신호를 디버그 빌드에서 adb 브로드캐스트로 넣음 — 화면 「시연 주입」·로그 `CommaInject` | fake, mock |
| 미리 준비 | `preload`·`standby` | 다음에 쓸 클립(두 문장 앞까지)의 플레이어를 소리 없이 준비해 둠(재개 = 시작만) | prefetch, cache |
| 놓기 | `retire`·`release`·`PlayerHandoff` | 다 쓴 플레이어를 다른 스레드에서 release — 시계로 끝난 문장의 것은 다음 준비 뒤에 | dispose, free |
| 리허설 | `demo/rehearsal.py` · `CommaStage` 로그 표지 | 새 설치부터 대본 3컷까지 자동으로 돌리고 녹화 — 두 번 돌려 같은지 본다 | dry run |
| 소리 시작 | `onSoundStarted`·`CommaPlayer start` | 플레이어가 실제로 소리를 낸 순간 — ⏸ 끄기·대본 anchor 기준. 엔진 `clipStart`(명령 순간)와 다름 | — |
| P 화면 | `ParkScreen`·`ScreenUiState.Park` | 정차(기어 P, 기어 확인 안 됨 + 멈춤) 화면 — 조작은 여기서만 | home, main |
| D 화면 | `DriveScreen`·`ScreenUiState.Drive` | 주행(D·R·N 또는 이동 중) 화면 — 글자·터치 0, 쉼표 중 ⏸ 하나 | driving mode |
| 권한 안내 | `PermissionScreen`·`AskSpeedPermission`·`SpeedPermission` | 첫 실행 정차 중 속도 권한을 한 번만 묻는 화면 | onboarding |
| 사용법 | `HowItWorks`·`StepRow` | P 화면 왼쪽 세 줄(레버 → 쉼표 → 재개) | tutorial |
| 표 | `ticket`·`ClipRequest` | 재생 요청 번호 — 늦게 끝난 준비·콜백이 지금 요청 것인지 가림 | token, id |

## 4. 설계서 이름 ↔ 코드 이름

| 설계서(SPEC) | 코드 |
|---|---|
| `ReaderState` + `reduce` | `reader/ReaderState.kt`(입구) — 이벤트별 처리는 `reader/OnCarEvent.kt`·`OnClipEnded.kt`·`OnPress.kt`·`OnFocusChanged.kt`·`OnTick.kt`, 공통 단계 이동은 `reader/ReaderMoves.kt` |
| phase `Stopped / Reading / Holding / FocusPaused` | `reader/ReaderPhase.kt` — `armedAt`은 `Reading` 안, `offSince`·`HoldReason`은 `Holding` 안(그 단계에서만 뜻이 있어서) |
| `holdReason` | `HoldReason.SENTENCE_END`(재개 = 다음 문장) · `MID_SENTENCE`(재개 = 같은 문장 처음부터) |
| `PlayClip(index, fromStart)` | `Effect.PlayClip(index, isFromStart)` (Boolean 이름 규칙) |
| `log/ToLogLine.kt` | `log/ReaderLog.kt`(줄 종류마다 `line`) + `log/LogFormat.kt`(`toWriteLog`·숫자 형식) |
| `ClassifyPackProblems` 문제 4종 | `PackProblem` 5종 — `BLANK_CREDIT`(크레딧 없음) 추가 |
| `Tick` 100ms | 앱이 `nextDeadline` 시각에 1회 — 위 1절 |

## 5. 패키지 지도 (✅ 있음 · ○ 예정)

```
engine/src/main/kotlin/app/blinkercomma/engine/
  signal/   ✅ CarEvent · TurnSignal · Gear                       — 차 신호 입력
  comma/    ✅ CommaCardParams · CommaSnapshot · classifyArm · ArmDecision — 쉼표 예약 판정(R1·R3)
  reader/   ✅ reduce · ReaderState · ReaderEvent · ReaderPhase · 이벤트별 처리 — 낭독 상태(R1~R10)
  output/   ✅ Effect · Transition                                 — 엔진 출력
  tally/    ✅ ReadingTally                                        — 오늘 카운트
  pack/     ✅ SentencePack · toPackFile · classifyPackProblems    — 콘텐츠 팩
  log/      ✅ ReaderLog · toWriteLog                              — 로그 줄(태그 Comma)
engine/src/test/kotlin/app/blinkercomma/engine/
  support/  ✅ replay · ReaderScene(기본 장면) · StageScript(TSV) · StageRun(앱 흉내 리플레이)
  scenario/ ✅ Cut1·Cut2·Cut3 + EdgeScenarioTest
app/src/main/kotlin/app/blinkercomma/
  (루트)    ✅ BlinkerApp · AppContainer(수동 DI) · Clock(monotonicNow) · MainActivity
  vehicle/  ✅ VehicleMapping · PleosVehicleSource(깜빡이·기어, 해제 안 함) · CarSpeedSource(권한 먼저 확인)
  session/  ✅ ReaderSession(엔진 상태·효과·⏸) · DeadlineTicker(nextDeadline 1회 깨우기) · DemoInjectionMarks
  player/   ✅ ClipPlayer(문장 끝 = 팩 길이 시계) · ClipDeck(열기·시작·멈춤 전용 스레드) · ClipStandby(미리 준비 — 자기 스레드, 두 문장 앞까지) · PlayerHandoff(놓기 순서) · PlayingClip
  service/  ✅ ReaderService(MBS + MediaSession + 포그라운드) · carEvents · ReaderMediaSession · AudioFocusGate · EffectRunner · ReaderNotification
  data/     ✅ PackRepository · TallyStore · ReaderSettings(자동 재개)
  ui/       ✅ ScreenUiState · ScreenInputs(toUiState — 어느 화면인가) · collectScreenUiState · AppScreen
  ui/park/  ✅ ParkScreen(P 화면) · PlaybackPanel(오른쪽 판) · TodayTally · HowItWorks(사용법 세 줄)
  ui/drive/ ✅ DriveScreen(D 화면 — 쉼표 중 ⏸ 하나)
  ui/permission/ ✅ PermissionScreen(첫 실행 속도 권한 안내)
  ui/component/ ✅ 화면 부품(아래 8절)
  ui/theme/ ✅ CommaColors · CommaType · CommaSpace · CommaSize · CommaShape · CommaTheme · calcContrastRatio
app/src/debug/kotlin/app/blinkercomma/debug/  ✅ DemoInjectReceiver · toInjectedEvent (디버그 빌드 전용)
app/src/main/assets/packs/en1/  ✅ 퍼블릭 도메인 낭독 팩(LibriVox, 38문장) + CREDITS.txt
tools/      ✅ measure_signals.py · measure_plans.py · device.py(실측) · make_probe_pack.sh(측정용, 교체 전 썼음) · cut_pack.py(STT 타임스탬프로 문장 자르기)
demo/script/ ✅ cut1_breath · cut2_wait_go · cut3_key_resume (TSV)
demo/        ✅ conductor(한 컷 재생 · anchor = 실제 소리 시작) · rehearsal(새 설치→권한→3컷 + 녹화) · rehearsal_summary(두 번 같은지) · script_rows · log_watch
```

## 6. 대본 형식 (`demo/script` 폴더의 TSV)

`at_s ⇥ hand ⇥ signal ⇥ value ⇥ memo`

- `at_s`: 숫자 = 바로 위 anchor부터 초(첫 anchor 전은 준비) · `end-X` = anchor 문장이 끝나기 X초 전(문장 길이는 팩에서) · `-` = anchor 줄
- `hand`: `vc`(Virtual Console) · `app`(P 화면 — 녹화 전 준비) · `adb`(시연 주입) · `anchor`(로그에 value 글자가 새로 찍힐 때까지 기다렸다가 그 시각을 0으로)
- `signal`: `turn_signal`·`gear`·`speed`(VC 값 km/h — 엔진엔 m/s로) · `play` · `auto_resume` · `media_key`(`PLAY_PAUSE` — MediaSession 기본 처리대로 재생 중이면 정지, 아니면 재생) · `focus`(테스트 전용)
- memo의 「숨은 조작」 줄마다 「빠뜨리면 이렇게 깨진다」 테스트가 하나씩 있다.

## 7. 외부 문서 번호

| 번호 | 무엇 |
|---|---|
| T9·T11·T1 | 저장소 밖 에뮬레이터 실측 표(setup T_results 2026-10-25) — T9 깜빡이 값=레버 값, T11 TTS 불가, T1 기어 서비스 수신 |
| R1~R10 | `docs/SPEC.md` 「쉼표 규칙」 표 |
| 원칙 1~11 | `docs/SPEC.md` 「반드시 지킬 원칙」 |

## 8. 화면 부품 (`ui/component` — 화면 파일은 이것만 조립한다)

| 부품 | 하는 일 |
|---|---|
| `BrandHeader` | 호박색 쉼표 + 앱 이름 + 스토어 한 줄(런처 아이콘과 같은 쉼표) |
| `RoundPlayButton` | P 화면 재생·정지 원 버튼(232dp, 그림 + 낭독기 이름) |
| `HoldGlyph` | D 화면 ⏸ 96dp(칸을 꽉 채운 `ic_hold`) |
| `TallyNumber` | 오늘 카운트 숫자 하나 + 라벨(낭독기는 한 줄) |
| `SettingToggleRow`·`ToggleRowText` | 켬·끔 설정 한 줄 — 줄 전체가 누르는 곳(120dp), 스위치 + 「켬/끔」 글자 |
| `StatusLine` | 「확인 안 됨」 신호 한 줄(빈 원 + 흐린 글자) |
| `StepRow` | 사용법 한 줄(원 안 그림 + 굵은 말 + 덧말) |
| `BigButton` | 글자 버튼(120dp, 호박색) |
| `DemoInjectedTag` | 「시연 주입」 레몬색 테두리 표시(디버그 빌드에서 주입한 뒤만) |

- 화면 크기 기준: 앱 창 1682×1210dp(2560×1440·160dpi 화면에서 시스템 막대·왼쪽 운전 화면을 뺀 자리 — D3 `dumpsys window` 실측). 측후방 카메라는 앱 창 왼쪽 위 754×424dp(D3 캡처 실측) → `CommaSize.cameraOverlayWidth` 780dp.
- 색은 호박색(깜빡이 색) 한 가지 강조 + 어두운 바탕. 대비 하한은 `CommaThemeFloorTest`.
