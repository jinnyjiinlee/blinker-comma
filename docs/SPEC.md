# 깜빡이 쉼표 (Blinker Comma) — 개발 설계서 v1

S0 · 2026-11-02. 기획 정본 = `../ideas/v3/meeting/DECISION.json`(출품작 결정) + `cards/MiaM22-1_v2.md`(카드) + `M6/demo·spike·biz·store_MiaM22-1.md` + `M7/summary.md`·`a_MiaM22-1.md`(결선 질의응답에서 바뀐 것). 숫자·출처는 거기 것만 쓴다. 이 설계서가 그 문서들과 다르면 **이 설계서가 개발 기준**이고, 다른 이유는 「기획과 달라진 점」 절에 적는다.
공모전: 접수 11/15(일) 23:59 · 앱 시연 동결 11/8 · 촬영 11/10 · Android API 28+ · Kotlin · Gradle 8+.
※ 저장소 밖 경로(`../`)는 근거 표기용이다 — 제출(P1) 전에 정리한다.

## 한 줄
**깜빡이를 올리면 낭독이 그 문장 끝에서 숨을 참고, 레버가 돌아오면 다음 문장 — 화면도 손도 없이.**
(스토어 한 줄 24자: 「깜빡이 켜면 낭독이 문장 끝에서 숨을 참아요」)

## 누가 · 매주 · 어떤 순간
- **누가**: 혼자 출퇴근하며 차에서 영어 낭독·오디오북을 트는 운전자(카드 P1).
- **매주**: 출퇴근 주 10회, 하루 73.9분(카드 「누가」 절, FACTS ③-1).
- **순간**: 차로를 바꾸려고 깜빡이를 올리는 몇 초. 그때 듣던 문장을 놓치면 되감는다(카드 「약점」·M7 Apple 답의 Pocket Casts 원문 — 「문장이 중간에 잘리면 되감는다」까지만 근거).
- **앱이 하는 일**: 레버가 올라간 채 문장 끝을 지나면 거기서 멈추고(⏸ 하나), 레버가 0.8초 내려가 있으면 다음 문장을 처음부터 읽는다.

## 쓰는 신호 (Pleos Vehicle SDK 2.3.2 + AAOS — `../setup/vehicle_sdk_api.md`)
근거 줄은 실측 로그. 「있음」 = 에뮬에서 같은 리스너로 콜백을 받아 봄 / 「조건부」 = 문서 근거만 있거나 우리 앱 기준 미실측.
**D2: 이 앱 기준 실측 끝 — 정본 `docs/signals_measured.md`**(신호별 ✅/❌·지연·권한 최소 집합·컷 1 모양 2회). 아래 판정은 그 결과로 갱신.

| 신호 | API | VC로 조작 | 실측 근거 | 판정 | 쓰임 |
|---|---|---|---|---|---|
| 깜빡이 LEFT/RIGHT/OFF | `TurnSignal.registerTurnSignalState` → `onTurnSignalStateUpdated(TurnSignalValue)` | ✅ `PUT /vehicle/turn-signal?mode=BlinkLeft·BlinkRight·Off` | `../ideas/v3/meeting/M6/spike_raw/logcat_turnsignal.txt` 3~6행(LEFT·OFF·RIGHT·OFF, 4토글 4콜백) · `../setup/rehearsal_15_logcat_run2.txt` 109행 RIGHT · 161행 OFF | **있음** | 쉼표 트리거(ON) · 재개 트리거(OFF) |
| 기어 | `Gear.registerGearSelection` → `onSelected(VehicleGear)` | ✅ VC 기어 | `../setup/T_results_1025.md` T1 행 「포그라운드 서비스에서 수신」 | **있음** | D가 아니면 쉼표 안 함 · P/D 화면 전환 |
| 차속 | AAOS `CarPropertyManager` `PERF_VEHICLE_SPEED`(m/s), 권한 `android.car.permission.CAR_SPEED`(dangerous, 런타임 허용 — 에뮬은 `pm grant --user 10`) | ✅ VC `controls?speed=`(km/h 정수) | `spike_raw/logcat_speed.txt` 3행 `speed mps=22.222223`(80km/h) · run2 108→112행(0 → 8.33m/s) | **있음** | 속도 0이면 쉼표 안 함 |
| 핸들 미디어 키 | `MediaSessionCompat.Callback` `onPlay/onPause/onSkipToNext` | ❌ VC 카드 없음 → `adb shell input keyevent 85` = **시연 주입** | D2 ✅ 우리 세션이 `code=85` 수신 · 재생 중 → onPause · 멈춤·쉼표 중 → onPlay(키 → 콜백 약 0.3초, 더블탭 대기) — `docs/logs/d2_signals_logcat.txt`·`d2_cut3_logcat.txt` | **있음**(입력은 시연 주입) | 폴백 재개 · 「자동 재개 끔」 모드의 재개 |
| 오디오 포커스(내비 안내 등) | `AudioManager.requestAudioFocus` / `OnAudioFocusChangeListener` | ❌ VC 없음 → 다른 앱의 실제 요청 또는 디버그 주입(시연 주입) | D2 ✅ 다른 앱이 안내 용도로 3초 요청 → LOSS_TRANSIENT 멈춤 → GAIN 멈춘 자리부터 — `d2_signals_logcat.txt` | **있음** | 남이 포커스를 가져가면 멈춤, 돌려주면 재개(표준 매너) |
| 클립 재생 완료 | `MediaPlayer.setOnCompletionListener` | — | D2 ❌ `-no-audio`에서 늦게도(4.0초 클립에 4.85초) 이르게도(3.0초 클립에 2.23초) 옴 | **안 씀**(기록만) | 문장 끝 = 팩 `durationMs` 단조 시계(로그 `by=clock`). 콜백은 `completion callback` 줄로 실차 비교용 기록 |

**안 쓰는 것**: 조향각(run2 전 구간 -0.0뿐, 각도 변화 미실측) · 브레이크 · 문·좌석(SX1 「트리거 확장」은 발전 단계) · 전조등/램프 상태 · TTS(T11 ❌ — 「Android 표준 TTS 초기화 -1」) · 위치 · 네트워크.
**권한**(D2 실측 최소 집합): `pleos.car.permission.CAR_LIGHTS`(없으면 깜빡이 등록 SecurityException) + `CAR_DRIVE`(없으면 기어 등록 SecurityException) + `android.car.permission.CAR_SPEED`(런타임 — 없으면 AAOS가 예외 없이 조용해서 앱이 먼저 권한을 보고 「확인 안 됨」) + `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` + `POST_NOTIFICATIONS`.

## 반드시 지킬 원칙
1. **주행 중 조작 0.** D(주행) 화면엔 터치할 것 0 · 글자 0 · 새 화면·팝업 0. 보이는 건 쉼표 중 ⏸ 하나뿐. 설정·재생 시작·숫자 보기는 전부 P(정차)에서.
2. **차량 제어 0.** 읽기만 한다.
3. **실명 0 · 계정 0 · 네트워크 0.** 코드·문서·커밋·리소스·APK·영상에 실명/회사명/연락처/로컬 경로 0. 로그인 없음, 콘텐츠는 APK 안 에셋.
4. **지어내기 0.** P 화면 「오늘 N문장 · 쉼표 M번」은 실제 카운트(고정 숫자 금지). 못 받는 신호는 「확인 안 됨」 — 예: 속도 권한 거부/미수신이면 P 화면에 「속도 신호 확인 안 됨」 한 줄, 속도 예외 없이 레버+D만으로 동작.
5. **자동 재생 금지.** 앱 실행·미디어 연결 때 재생을 시작하지 않는다(Pleos 「앱 연결 시 자동 재생은 금지」). 첫 재생은 운전자가 P에서 ▶ 또는 핸들 키. 쉼표 뒤 자동 재개는 같은 세션 안의 일시정지 복귀다 — Pleos 문언상 [해석] 1줄로 남기고, **「자동 재개 끔」 모드(핸들 키로만 재개)**를 P 설정에 둔다.
6. **문장 본문은 시스템 화면에 안 띄운다.** MediaSession 메타 제목 ≤6자(예: 「낭독 1과」), 부제 0, 문장 텍스트 0(시스템 미디어 카드에 그대로 뜨므로).
7. **쉼표는 우리 플레이어 직접 pause/play로 만든다.** 오디오 포커스 왕복을 쉼표 수단으로 쓰지 않는다(카드 쉼표 규칙 3 — 대역 앱 1회 1.4초 반례).
8. **디바운스는 시연 앱 = 출품 앱.** 0.8초 상수 1개(`CommaCardParams.offDebounce`). 42dot 회신이 「스위치 값」이면 0으로 바꾸되 코드는 하나.
9. **Pleos 리스너는 한 번 등록하면 해제하지 않는다**(T_results 「문 리스너를 해제하자 기어·깜빡이 콜백까지 끊김」).
10. **시연 정직 표기.** VC 조작은 실제 신호 경로. VC로 못 넣는 핸들 키는 `adb keyevent` = 「시연 주입」 — README·영상 자막·로그에 표기. 그 밖에 VC로 못 넣는 신호(오디오 포커스 등)는 **디버그 빌드 전용 주입 브로드캐스트**로만 넣고, 넣은 순간부터 화면에 노란 「시연 주입」 + 로그 `CommaInject`(D2 — 릴리스 APK엔 수신기가 없어 D 화면 글자 0 원칙은 출품 앱에서 그대로). 에뮬은 `-no-audio` → 영상의 낭독 소리는 편집 합성, 자막 1회 「소리는 편집 합성 — 시각은 앱 로그 기준」.
11. **화면 배치**: 깜빡이 때 시스템 측후방 카메라가 앱 창 왼쪽 위를 덮는다(T_results 「약 750px」, D3 캡처 실측 754×424dp) → ⏸는 x ≥ 780dp, P 화면의 누르는 것도 그 자리 밖.

## 쉼표 규칙 (엔진이 지키는 것 — 카드 「쉼표 규칙 3줄」 + M7 수정)
| # | 규칙 | 값 | 출처 |
|---|---|---|---|
| R1 | 레버 ON + 기어 D + 이동 중이면 「쉼표 예약」. 예약된 채 문장 끝(클립 끝)에 닿으면 다음 클립을 시작하지 않고 멈춘다 | 이동 = 속도 > `movingMinMps` 0.3m/s [설계 — VC 최소 1km/h=0.28m/s보다 위] | 카드 규칙 1 |
| R2 | 문장 끝 전에 레버가 OFF로 돌아오면 예약 취소(쉼표 0) | — | 카드 규칙 1 |
| R3 | 속도 0에서 레버 ON → 예약 안 함. 레버를 켠 채 출발하면 이동이 시작된 순간 예약 | — | 카드 규칙 1 · 컷 2 |
| R4 | 예약 후 `endWaitLimit` 안에 문장이 안 끝나면 그 자리에서 즉시 멈춤(문장 중간). 재개 땐 **그 문장 처음부터** [설계 — 잘린 문장을 건너뛰지 않게] | 2.5초 | 카드 규칙 2 |
| R5 | 멈춘 뒤 레버 OFF가 `offDebounce` 동안 유지되면 다음 문장(R4로 멈췄으면 같은 문장 처음부터) 재생. 그 사이 다시 ON이면 타이머 리셋 | 0.8초 | M7 a_MiaM22-1 Mason Q1 답 「OFF 3.7 + 0.8 = 4.5」 |
| R6 | 쉼표는 레버 한 번(ON 구간)당 최대 1번 → 쉼표 수 ≤ 레버가 켜진 채 문장 끝을 넘긴 횟수 | — | 카드 규칙 1 |
| R7 | 멈춘 동안 정차·기어 변경이 와도 멈춤 유지(레버 OFF 또는 운전자 ▶/핸들 키로만 풀림) [설계 — 예측 가능성] | — | — |
| R8 | 「자동 재개 끔」 모드면 R5 대신 핸들 키(또는 P의 ▶)로만 재개 | 기본 = 켬 | demo 컷 3 |
| R9 | 멈춘 중 핸들 키 PLAY/PLAY_PAUSE = 즉시 재개(디바운스 없음) | — | demo 컷 3 「엄지 한 번」 |
| R10 | 다른 앱이 포커스를 일시로 가져가면(LOSS_TRANSIENT) 멈추고, 돌려주면(GAIN) 쉼표 중이 아닐 때만 재개. 영구 LOSS면 정지 | — | Pleos 포커스 가이드 기본 매핑 |

- ⏸는 **소리가 실제로 멎는 순간** 켜지고 다시 날 때 꺼진다(명령 순간 아님).
- 로그(영상 PiP·실측용, 태그 `Comma`) — 형식 고정, 엔진의 `toLogLine`이 만든다:
  - ⓐ `turnSignal=RIGHT` ⓑ `clipEnd s=A lever=ON → hold` ⓒ `turnSignal=OFF` ⓓ `OFF held 0.80s → play s=B`
  - 보조: `lever=ON speed=0 → no comma` · `moving → comma armed` · `endWait 2.50s → hold mid s=A` · `mediaKey PLAY → play s=B` · `autoResume=off → keep hold`

## engine 부품 (`:engine` 순수 Kotlin — `android.*`·코루틴·시계 없음, 테스트 먼저)
road-interpreter `engine/signal/*`·`output/Transition`·테스트 `support/Replay.kt`를 복사·축소해 시작한다(원본 수정 0).

| 파일(패키지) | 무엇 | 테스트 핵심 |
|---|---|---|
| `signal/CarEvent.kt` · `TurnSignal.kt` · `Gear.kt` | `TurnSignalChanged(at, signal)` · `GearChanged(at, gear)` · `SpeedChanged(at, speedMps)` (복사) | 매핑은 app 쪽 |
| `reader/ReaderEvent.kt` | 엔진 입력 전체: `Car(CarEvent)` · `ClipEnded(at, index)` · `PlayPressed(at, source)` · `PausePressed(at, source)` · `SkipPressed(at, step)` · `FocusChanged(at, focus)` · `Tick(at)` | — |
| `comma/CommaCardParams.kt` | `offDebounce 0.8s` · `endWaitLimit 2.5s` · `movingMinMps 0.3` · `isAutoResume true` | 기본값 고정 테스트 1개(위 표 숫자) |
| `comma/CommaSnapshot.kt` · `ClassifyArm.kt` | `classifyArm(snapshot, params)` → `Armed / SkipStopped / SkipNotDrive / LeverOff` | 속도 문턱 경계 3점 · 기어 P/R/N |
| `reader/ReaderState.kt` | `data class ReaderState(phase, index, lever, gear, speedMps, armedAt, offSince, holdReason, tally)` + `fun reduce(state, event): Transition` — phase = `Stopped / Reading / Holding / FocusPaused` (sealed) | R1~R10 각각 · 디바운스 경계 3점(0.79·0.80·0.81s) · 2.5초 상한 경계 3점 |
| `output/Effect.kt` · `Transition.kt` | `PlayClip(index, fromStart)` · `PauseClip` · `ShowHoldGlyph(isOn)` · `WriteLog(line)` · `SaveTally` | — |
| `tally/ReadingTally.kt` | `ReadingTally(sentences, commas)` — 문장 완료·쉼표 카운트. 날짜 키는 app이 넘김(엔진은 날짜를 모름) | 쉼표 수 ≤ 레버 ON 구간 수 |
| `pack/SentencePack.kt` · `PackFile.kt` · `ClassifyPackProblems.kt` | 콘텐츠 팩 JSON(kotlinx.serialization): `title`(≤6자) · `credit` · `clips[{file, text, durationMs}]` → 문제 목록(제목 6자 초과·클립 0개·길이 0·파일 중복) | 정상 `validJson` + 한 칸씩 깨기 |
| `log/ReaderLog.kt` · `LogFormat.kt` (D1: ToLogLine → 줄 종류마다 `line`) | 이벤트·효과 → 위 로그 줄 문자열 | ⓐ~ⓓ 문자열 그대로 |
| 테스트 `scenario/Cut1…Cut3Test.kt` + `CancelBeforeEndTest` · `MidSentenceHoldTest` · `FocusLossTest` | `demo/script/cut*.tsv`를 그대로 읽어 엔진 리플레이 → 효과·로그 줄·카운트 비교 | 매 커밋 |

## app 부품 (`:app`)
| 부품 | 무엇 | 출발점 |
|---|---|---|
| `vehicle/PleosVehicleSource.kt` · `VehicleMapping.kt` | 깜빡이·기어 콜백 → `Flow<CarEvent>` (조향·문·좌석 제거) | road-interpreter 복사·축소 |
| `vehicle/CarSpeedSource.kt` | AAOS 속도 → `SpeedChanged` | road-interpreter 복사 |
| `player/ClipPlayer.kt` · `ClipDeck.kt` | 문장 끝 = 팩 길이 시계 → `ClipEnded`. 재생마다 새 `MediaPlayer`(전용 스레드 `ClipDeck`), 멈춤 = 놓고 자리 기억, 다음 클립 미리 준비 · 놓기는 별도 스레드(D2 — 에뮬레이터 준비·놓기 막힘 대응, `docs/signals_measured.md`) | road-interpreter `VoiceOut.playRecording` 참고 |
| `service/ReaderService.kt` | `MediaBrowserServiceCompat` + `MediaSessionCompat`(Pleos Music/Audio 필수 MBS). 신호·플레이어·키 이벤트 → `reduce` → 효과 실행. 재생 시작 때 `startForeground`. `Tick`은 `ReaderState.nextDeadline(params)` 시각에 1회(D1 변경 — 100ms 폴링이면 로그 `OFF held`가 0.80~0.89로 흔들림) | road-interpreter `RoadWatchService` 구조 참고 |
| `service/AudioFocusGate.kt` | 재생 시 GAIN 요청(`setWillPauseWhenDucked`), 변화 → `FocusChanged`, 정지에서만 반납 | — |
| `session/ReaderSession.kt` | 엔진 상태 한 곳(메인 스레드) · 효과 실행 · ⏸ 끄기는 플레이어의 실제 소리 시작까지 미룸 | — |
| `debug/DemoInjectReceiver.kt` (src/debug) | 시연 주입 브로드캐스트 → 엔진 이벤트 + 「시연 주입」 표시(원칙 10) | — |
| `data/PackRepository.kt` · `TallyStore.kt` | 에셋 팩 로드 · 오늘 카운트 저장(SharedPreferences, 날짜 키) | — |
| `ui/` (Compose) | 아래 화면 3개 + 테마 + 부품(`docs/ARCHITECTURE.md` 8절). 어느 화면인지는 `ScreenInputs.toUiState`(JVM 테스트). `distractionOptimized` 메타 | road-interpreter `ui/component`·`theme` 참고 |

## 화면 목록 (차량 화면 큰 글씨 · 어두운 테마 · 고대비 — D3 확정, 캡처는 진행 기록 D3)
앱 창 1682×1210dp(실측). 강조색은 깜빡이 호박색 하나(`CommaColors.SIGNAL`), 바탕은 거의 검정. 가장 작은 글자 28sp(하한 24sp) · 대비 4.5 이상(햇빛 모델 3 이상) · 터치 120dp — `CommaThemeFloorTest`. 애니메이션 0.
1. **P 화면(정차 — 기어 P, 또는 기어 확인 안 됨 + 멈춤)** — 왼쪽: 앱 표지(호박색 쉼표 + 이름 + 스토어 한 줄) / 사용법 세 줄(깜빡이를 켜면 → 문장 끝에서 쉬고 → 레버가 돌아오면) / 「오늘 **N** 문장 · **M** 쉼표」 숫자 132sp·라벨 34sp / 「자동 재개 켬·끔」 줄 1개(설명 한 줄이 켬·끔에 따라 바뀜) / 콘텐츠 출처 1줄. 오른쪽 판(560dp): 팩 제목 / 재생·정지 원 버튼 232dp(그림) / 지금 단계(멈춤·읽는 중·쉼표 중·다른 소리 대기 중)와 「n / 전체 문장」 / 신호 상태(못 받은 것만 「○ 속도 신호 확인 안 됨」) / 시연 주입. 돈·구독사 이름 0자.
2. **D 화면(주행 — 기어 D·R·N, 또는 기어를 몰라도 속도 > 0.3m/s)** — 어두운 빈 화면. 쉼표 중에만 ⏸ 96dp 1개, 카메라 폭 오른쪽 자리의 가운데(실측 x 1197~1263dp). 글자 0 · 터치 0. 시연 주입 표시만 디버그 빌드에서.
3. **권한 안내(첫 실행, 정차에서만, 한 번만)** — 「신호 대기 중엔 멈추지 않도록 차 속도를 읽어요」 + 「허용하지 않아도 깜빡이와 기어만으로 동작해요」 + 「시스템 창에는 「위치 정보」 권한으로 보여요」(AAOS가 CAR_SPEED를 위치 묶음으로 물음 — D3 캡처) + [허용] 1버튼. 시스템 창에서 거부해도 다시 묻지 않고 P 화면(원칙 4). 주행 중이면 정차까지 미룸.
- 런처 아이콘: 어두운 타일 + 호박색 쉼표 하나(적응형 아이콘, 앱 표지와 같은 모양).
- 시스템 미디어 카드: 우리 화면 아님 — 메타 제목 ≤6자만 관리(원칙 6).

## 콘텐츠 팩 (영어 낭독 1팩 · APK 에셋)
- **문장 1개 = 클립 1개** → 문장 경계 = 클립 끝(마킹 오류 0, 카드 「문장 경계」 절).
- 분량: 30~40문장, 문장당 2~6초. 컷 1용으로 **길이가 정해진 문장 A·B**가 필요(앵커 방식이라 정확한 길이는 대본이 팩에서 읽음).
- **출처 결정(S0)**: TTS 불가(T11 ❌)라 사람 낭독 녹음이 필요하다. 기본안 = **퍼블릭 도메인 영어 낭독(LibriVox 등 PD 공개 녹음)을 문장 단위로 잘라 쓰고 P 화면·README에 크레딧**. 녹음자 섭외가 필요 없고 저작권이 깨끗하다. 대안 = 직접 녹음(녹음자 [미확인]). → 카드·스토어의 「자체 녹음」 문구는 「퍼블릭 도메인 낭독」으로 바뀐다(아래 「기획과 달라진 점」).
- 자르기: 오프라인 스크립트(`tools/cut_pack.py`). D5: 무음 구간 검출 대신 word-level STT 타임스탬프(whisper)로 문장 끝(.!?)을 찾는다(자막이 실제 발화와 안 어긋남) → `assets/packs/en1/pack.json` + `s01.m4a…`. 원본 출처 URL·라이선스는 `assets/packs/en1/CREDITS.txt`.

## 시연 3컷 대본 (Virtual Console로 재생 — `demo/script/cutN_*.tsv`)
형식: `at_s ⇥ hand ⇥ signal ⇥ value ⇥ memo` (road-interpreter 형식). D1 확정: `at_s`는 숫자(바로 위 anchor부터 초) · `end-X`(anchor 문장이 끝나기 X초 전, 길이는 pack.json) · `-`(anchor 줄) — 세부는 `docs/ARCHITECTURE.md` 6절. hand = `vc`(VC 웹 API) · `app`(P 화면 탭 — 녹화 전 준비만) · `adb`(키 주입 = 시연 주입) · **`anchor`(새로 추가: logcat에 해당 줄이 찍힐 때까지 기다린 뒤 그 시각을 0으로 다시 잡음)**. conductor는 road-interpreter `demo/conductor.py`를 복사해 `anchor` 행을 추가한다. 같은 TSV를 시나리오 테스트가 읽는다(anchor는 테스트에선 해당 이벤트 시각으로 치환).
컷 1·2 = 1분 영상, 컷 3 = 2차 PT 15초 시연(영상 밖 — M6 사회 결정).

### 컷 1 — 첫 5초 「숨 참기」 (영상 0.0~5.0초)
준비: 새 설치 → 권한 → P에서 ▶(녹화 전) → VC 기어 D · 속도 40.
| 대본 | 손 | 신호 | 기대(앱 로그 · 화면) | 영상 시각 |
|---|---|---|---|---|
| anchor | — | `clipStart s=A` | 문장 A 시작 | — |
| A끝 − 0.6s | vc | turn-signal BlinkRight | ⓐ `turnSignal=RIGHT` · `moving → comma armed` | 2.0 |
| A끝 | — | (클립 끝) | ⓑ `clipEnd s=A lever=ON → hold` · ⏸ 켜짐 | 2.6 |
| +1.1s | vc | turn-signal Off | ⓒ `turnSignal=OFF` · ⏸ 유지 | 3.7 |
| +0.8s | — | (디바운스) | ⓓ `OFF held 0.80s → play s=B` · ⏸ 꺼짐 | 4.5 |
- 검증: 소리 사건 2개(멎음 2.6 · 다음 문장 4.5) 모두 VC 토글이 원인, 모의 0. 계산: 3.7 + 0.8 = 4.5 ≤ 5.0.
- 미실측 가정: VC 토글 간격 1.7초에서 콜백 2개가 다 오는지(실측 간격은 3.0초·6.04초뿐) → D2/D4 리허설에서 실측, 안 되면 문장 A를 짧은 것으로 바꿔 BlinkRight를 앞당긴다(재개 4.5초 유지).

### 컷 2 — 신호 대기 → 출발 (12초)
준비: D · 속도 0 · 낭독 중.
| 대본 | 손 | 신호 | 기대 |
|---|---|---|---|
| anchor | — | `clipStart` 아무 문장 | — |
| 1.0 | vc | turn-signal BlinkRight | `lever=ON speed=0 → no comma` · ⏸ 없음 · 낭독 계속 |
| 4.0 | vc | speed 30 | `moving → comma armed` |
| (그 문장 끝) | — | — | ⓑ hold · ⏸ 켜짐 |
| anchor | — | `→ hold` | — |
| 4.5 | vc | turn-signal Off | ⓒ |
| 5.3 | — | — | ⓓ 다음 문장 · ⏸ 꺼짐 |
- 근거: 같은 순서(속도 0 → RIGHT → 8.33m/s)가 run2 108→109→112행에 이미 있다.
- D1 대본(`demo/script/cut2_wait_go.tsv`): 출발은 「다음 문장 시작」 anchor + `end-1.0`으로 넣는다 — 고정 4.0초면 문장 길이에 따라 예약 뒤 2.5초 상한에 걸려 문장 중간에서 멈출 수 있다. OFF는 hold anchor + 0.5초, ⓓ는 +1.3초.

### 컷 3 — 「자동 재개 끔」 + 핸들 키 (2차 PT 15초, 영상 밖)
준비: P에서 「자동 재개 끔」 탭 → ▶ → VC 기어 D · 속도 40.
| 대본 | 손 | 신호 | 기대 |
|---|---|---|---|
| anchor | — | `clipStart` | — |
| 1.0 | vc | turn-signal BlinkRight | armed → 문장 끝 hold · ⏸ |
| 4.0 | vc | turn-signal Off | `autoResume=off → keep hold` · ⏸ 유지(글자 안내 0) |
| 6.0 | adb | `input keyevent 85` (**시연 주입** — 핸들 ▶❙❙ 대역) | `mediaKey PLAY → play s=…` · ⏸ 꺼짐 (D1: PLAY_PAUSE 키는 MediaSession이 멈춤 중이면 onPlay로 바꿔 줘서 엔진엔 PLAY로 들어옴) |
| 8.0 | vc | speed 0 → gear P | P 화면 「오늘 N문장 · 쉼표 M번」(실제 카운트) |
- D1 대본(`demo/script/cut3_key_resume.tsv`): BlinkRight는 `end-1.0`, OFF·키·정차는 hold anchor + 1.0·3.0·5.0/5.5초.
- 키 수신이 ❌이면(D2 실측) 컷 3은 접고 PT 답을 「MediaSession 표준 미디어 키 경로(문서)」로만 한다 — 그때 「자동 재개 끔」 모드의 재개 수단은 P의 ▶뿐이라 컷 3 영상화 불가.

## 11/8까지 범위 (MVP — 1인+AI 약 20시간)
| 단계 | 내용 | 시간 |
|---|---|---|
| D1 | 뼈대 복사(ride-seal) · engine 전 부품 + 단위·시나리오 테스트 · 빈 MainActivity · check.sh OK | 5h |
| D2 | 신호 어댑터 3종 · ClipPlayer · ReaderService(MBS+MediaSession+포그라운드) · 포커스 · 콘텐츠 팩 자르기 · 에뮬 실측 `docs/signals_measured.md`(깜빡이·기어·속도·완료 콜백·핸들 키·1.7초 간격) | 6h |
| D3 | 화면 3개 · 테마 · 런처 아이콘 · 스크린샷 검사 | 3h |
| D4 | conductor `anchor` 확장 · 대본 3컷 · 녹화 2회 동일성 · README(규정 4항목) | 3h |
| 여유 | 결함 수정·M1 대응 | 3h |

**MVP에 넣는 것**: 앱 안 영어 낭독 1팩 · 쉼표 규칙 R1~R10 · 「자동 재개 끔」 토글 · 핸들 키(MediaSession) · 오디오 포커스 표준 대응 · P/D/권한 화면 · 오늘 카운트 · 로그 줄 ⓐ~ⓓ · 대본 3컷 + conductor · release APK(D5).

**뺀 것** (기획서엔 「개발 계획」으로만):
- **외부 앱 모드**(윌라·멜론 등 다른 앱 소리를 레버로 멈추기) — 포커스 왕복이 원칙 7과 충돌 · 재생 안 하는 백그라운드 서비스의 마켓 허용 [미확인] · Mason 「2단계로 미루면 기술 13.5」(M7 summary 반대 기록 — 1단계 vs 2단계 미결을 개발 범위상 2단계로 정함)
- 구독사 연동·문장 경계 메타 · Gleo 「다시 읽어줘」 · 트리거 확장(급제동·후진·뒷문·좌석)
- 팩 여러 개·다운로드 · 화면에 문장 텍스트 · 배속 · 앱 재시작 뒤 이어듣기 · 영어 UI · 실제 소리 녹음(에뮬 `-no-audio`)

## 기획과 달라진 점 (개발 기준 — 제출 서류에 반영 필요)
| 기획 문서 | 바뀐 것 | 이유 |
|---|---|---|
| 카드·store 「영어 낭독(자체 녹음)」 | 「퍼블릭 도메인 영어 낭독」(출처 크레딧) | 녹음자 [미확인] · TTS 불가 · 20시간 범위. 직접 녹음이 생기면 팩만 교체 |
| M7 a_MiaM22-1 Kate Q1 「1단계에 외부 앱 모드」 | 뺌(개발 계획) | 위 「뺀 것」 첫 줄 |
| 카드 규칙 3 「재개 = 레버 OFF 즉시 ≤0.5초」 | OFF 0.8초 유지 후 재개 | M7에서 이미 정정(OFF 3.7 → 4.5) — 카드 v3 반영 대기 |
| 카드 규칙 2 상한 이후 동작 미정 | 상한 넘어 문장 중간에 멈추면 재개 땐 그 문장 처음부터 | 설계 결정 |
| demo 컷 2 「10.0초 Off → 재개」 | Off + 0.8초 뒤 재개 | 디바운스 반영 |
| 카드 「P: 2숫자 26dp」·설계서 초안 「라벨 20dp」 | 숫자 132sp·라벨 34sp | 차 화면 글자 하한 24sp(20 불가) · 1682dp 창에서 26dp 숫자는 흘끗 안 읽힘(D3 캡처) |

## 위험 · 미실측 (D2·D4에서 닫을 것)
- ✅(D2 닫힘) `-no-audio` 완료 콜백 → 믿을 수 없어 팩 길이 시계로 · 핸들 키 `keyevent 85` 수신 ✅(컷 3 유지) · VC 1.7초 간격 콜백 2개 ✅.
- ⚠️ 에뮬레이터 오디오: 플레이어 준비·놓기가 가끔 2~8초 막힘. D4: 준비를 소리 스레드에서 떼고 두 문장 앞까지 미리 준비 → 리허설 5·6회차 준비 막힘 6번(2.8~8.1초)에도 열기 31번 최대 27ms(`docs/progress.md` D4). 대본 anchor는 엔진 `clipStart`가 아니라 **`CommaPlayer start s=N`**(실제 소리 시작)에 건다(D4 conductor ✅).
- ⚠️ 컷 1 영상 시각: 측정용 팩의 문장 A(s=2)가 3.0초라 ⓓ = 4.9초(5.0초 안, 여유 0.1초). 낭독 팩에선 문장 A를 2.6초 근처로 골라야 대본 시각(ⓐ2.0 ⓑ2.6 ⓒ3.7 ⓓ4.5)과 맞는다.
- ⚠️ 앱 실행 직후 메인 스레드 약 4초 늦음 1/4회(원인 확인 안 됨) → 녹화 전에 앱을 띄워 둔다.
- 실차: 레버 자동 복귀 → OFF 이벤트 [짐작] · TURN_SIGNAL_STATE 램프/레버 모호(점멸 채터 → 디바운스로 방어 [가정]) · 레버 OFF 자동 재개의 Pleos 문언 해석 — 42dot 문의는 대표 승인 대기(발송 0).
- ✅(D5 닫힘) 콘텐츠 라이선스: LibriVox "The Aesop for Children" 섹션 13(archive.org `aesopforchildren_1308_librivox`) — archive.org 메타데이터 `licenseurl` = Public Domain Mark 1.0 확인, CREDITS.txt에 URL·sha256 기록.
