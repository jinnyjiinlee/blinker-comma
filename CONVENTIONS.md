# 코드 규칙 (android-app 스킬 기준)

- 함수 15줄(화면 Composable 30줄) · 파일 120줄(테스트 200) · 인자 4개(생성자 6·data class 제외) · 중첩 3단 — `./gradlew check` + `scripts/check.sh .`가 강제
- 네이밍: 판정 `classifyX` · 이벤트 `onX` · Boolean `is/has/can` · 단위 접미사 `Mps`·`M`·`Deg`, 시간은 `Duration`
- SDK는 `vehicle/` 어댑터로만, 판정은 순수 함수, 문턱값은 `CommaCardParams.DEFAULT`
- engine은 시계를 읽지 않는다 — 시간은 이벤트의 `at`, 타임아웃은 앱이 `nextDeadline`에 보내는 `Tick`
- 용어는 `docs/ARCHITECTURE.md` 용어 사전의 단어만

## 공통 부품 (새 코드 쓰기 전에 여기부터 본다)

| 부품 | 파일 | 하는 일 |
|---|---|---|
| `reduce(ReaderState, ReaderEvent, CommaCardParams)` | engine/reader/ReaderState.kt | 엔진 입구 — `Transition(state, effects)` 받기 |
| `ReaderState.nextDeadline(params)` | engine/reader/ReaderState.kt | 앱이 다음 `Tick`을 보낼 시각(null = 없음) |
| `classifyArm` | engine/comma/ClassifyArm.kt | 쉼표 예약 판정(레버·기어·속도) |
| `startReading`·`hold`·`resume`·`stop` | engine/reader/ReaderMoves.kt | 단계 이동 + 효과 묶음 — 새 이벤트 처리는 이걸 조합 |
| `Transition.then` | engine/output/Transition.kt | 상태를 이어 돌리며 효과를 순서대로 붙임 |
| `toWriteLog(ReaderLog)` | engine/log/LogFormat.kt | 로그 줄 효과 — 글자는 `ReaderLog.line` |
| `toPackFile`·`classifyPackProblems` | engine/pack/ | 팩 JSON 읽기·검사 |
| `toTurnSignal`·`toGear` | app/vehicle/VehicleMapping.kt | Pleos 값 → 엔진 값 |
| `ReaderSession.onEvent`·`onSoundStarted`·`holdSide` | app/session/ReaderSession.kt | 엔진에 이벤트 넣기(메인 스레드) · 실제 소리 시작 알림 · ⏸ 켜질 때 레버 방향 latch |
| `carEvents(context, vehicle)` | app/service/CarEvents.kt | 차 신호 3종 → 엔진 이벤트(실패한 신호는 「확인 안 됨」 로그) |
| `monotonicNow()` | app/Clock.kt | 이벤트 시각(단조) |
| `ClipPlayer.play`·`pause` | app/player/ClipPlayer.kt | 문장 재생·멈춤 — MediaPlayer는 직접 부르지 말고 이것만 |
| `demo/conductor.py`(`run_cut`)·`rehearsal.py`·`rehearsal_summary.py` | demo/ | 대본 한 컷 재생(anchor = 실제 소리 시작) · 새 설치→권한→3컷 녹화 · 두 리허설 비교 — 시연 순서를 바꾸면 TSV만 고친다 |
| `tools/device.py`·`measure_plans.py` | tools/ | 에뮬레이터 손(VC·adb·화면 누르기·주입) · 실측 순서표 — 화면 누르기는 P에서만(D 화면엔 누를 것 0) |
| `ScreenInputs.toUiState` | app/ui/ScreenInputs.kt | 어느 화면(P·D·권한 안내)을 무엇으로 그리나 — 화면은 판정 없이 그리기만 |
| 화면 부품 | app/ui/component/ | 목록은 `docs/ARCHITECTURE.md` 8절 — 화면 파일에서 `Text`·`Button`을 직접 꾸미지 않는다 |
| `CommaColors`·`CommaType`·`CommaSpace`·`CommaSize`·`CommaShape` | app/ui/theme/ | 화면 값은 여기만(밖에서 `dp`·`sp`·`Color` import 금지 — detekt) |
| `HoldGlyphTransition` | app/ui/theme/HoldGlyphTransition.kt | ⏸ 1회성 전환(애니메이션은 여기만 — 밖에서 `androidx.compose.animation.*` import 금지) |
| `replay`·`logs` | test/support/Replay.kt | 리듀서 리플레이(마지막 상태 + 효과 전부) |
| `ReaderScene` | test/support/ReaderScene.kt | 기본 장면(D·40km/h·2번째 문장 읽는 중) + 이벤트 도우미 + `holding` |
| `StageRun`·`readStageScript`·`parseStage` | test/support/ | 대본 TSV를 앱 흉내(클립 길이·마감 Tick)로 돌림 |

## 테스트 대장 (설계서 → 테스트 — 「미작성」이 0이 되면 지운다)

| 설계서 | 테스트 파일 | 상태 |
|---|---|---|
| CommaCardParams 기본값 | CommaCardParamsTest | 완료 |
| classifyArm 속도 경계 3점·기어 P/R/N·확인 안 됨 | ClassifyArmTest | 완료 |
| R1·R2·R3·R6 | ReaderArmTest | 완료 |
| R4 상한 경계 3점·중간 멈춤 재개 | ReaderEndWaitTest | 완료 |
| R5 디바운스 경계 3점·리셋·R7·R8·R9 | ReaderResumeTest | 완료 |
| R10 포커스 | ReaderFocusTest | 완료 |
| 원칙 5 자동 재생 금지·P/핸들 누름·건너뛰기 | ReaderPressTest | 완료 |
| 문장 = 클립·늦은 콜백·팩 끝·카운트 저장 | ReaderClipTest | 완료 |
| 쉼표 수 ≤ 레버 ON 구간 수 | ReadingTallyTest | 완료 |
| 팩 JSON·문제 5종(제목 경계 3점) | PackFileTest·ClassifyPackProblemsTest | 완료 |
| 로그 ⓐ~ⓓ·보조 줄 | ReaderLogTest | 완료 |
| 시연 컷 1·2·3 + 숨은 조작 | scenario/Cut1BreathTest·Cut2WaitGoTest·Cut3KeyResumeTest | 완료(합성 팩 — 실제 팩 길이로 D2에 다시 돌림) |
| R2 취소·R4 중간 멈춤·R10 내비 안내 장면 | scenario/EdgeScenarioTest | 완료 |
| 시연 주입 해석 | app/testDebug ToInjectedEventTest | 완료 |
| 재생 위치(단조 시계) | app/test PlayingClipTest | 완료 |
| 놓기 순서(끝난 문장은 다음 준비 뒤 · 멈춤은 바로) | app/test PlayerHandoffTest | 완료 |
| 리허설 2회 동일(로그 줄·시각 ≤ 0.15초) | demo/rehearsal_summary.py — 기기 리허설 | 기기 리허설 2/2(D4 5·6회차) |
| 화면 하한(글자 24sp·대비 4.5·햇빛 3·터치 76dp·⏸ 96dp) | app/test CommaThemeFloorTest | 완료 |
| 화면 고르기(P·D·권한 안내)·이동 문턱 경계 3점·확인 안 됨 줄·첫 신호 대기·쉼표 방향(LEFT·RIGHT) | app/test ScreenInputsTest | 완료 |
| ⏸ 진입 오프셋이 카메라 자리를 안 넘음(경계) | app/test CommaThemeFloorTest | 완료 |
| P 원 버튼(쉼표 중·포커스 멈춤 = ▶, R8) | app/test ScreenPressTest | 완료 |
| 화면 잘림·겹침 | 에뮬레이터 캡처(저장소 밖 build_shots 폴더 D3_*.png — JVM 테스트 아님) | 완료(D3 캡처 11장) |
| 어댑터·플레이어·서비스 | 에뮬레이터 실측 `docs/signals_measured.md`(기기 리허설 — JVM 테스트 아님) | 완료(D2 실측) |
| 시나리오 테스트를 실제 낭독 팩 길이로 | — | 미작성(낭독 팩 들어온 뒤) |
