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
| 어댑터·플레이어·서비스(에뮬 실측) | — | 미작성(D2) |
