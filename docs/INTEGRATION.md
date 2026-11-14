# 다른 오디오 앱에 「쉼표 규칙」 붙이기

깜빡이 쉼표의 판단은 전부 `engine` 모듈(순수 Kotlin, 안드로이드 의존 0) 안에 있다.
앱 화면·플레이어와 분리돼 있어서, 문장 경계를 아는 오디오 앱이면 같은 엔진을 그대로 가져다 쓸 수 있다.

## 1. 앱이 할 일 — 넣고, 받은 대로 한다

```kotlin
var state = ReaderState(clipCount = sentences.size)     // 문장(또는 구절) 수
val params = CommaCardParams.DEFAULT                     // 상한 2.5초 · 디바운스 0.8초 · 정차 문턱 0.3m/s

fun feed(event: ReaderEvent) {
    val result = reduce(state, event, params)            // 판정은 엔진이
    state = result.state
    result.effects.forEach { effect ->                   // 앱은 받은 대로만
        when (effect) {
            is Effect.PlayClip -> player.playSentence(effect.index, fromStart = effect.isFromStart)
            Effect.PauseClip -> player.pause()
            is Effect.ShowHoldGlyph -> drivingScreen.showPause(effect.isOn)
            is Effect.WriteLog -> Log.i("Comma", effect.line)
            is Effect.SaveTally -> store.save(effect.tally)
        }
    }
}
```

| 넣는 것 (`ReaderEvent`) | 어디서 오나 |
|---|---|
| `Car(TurnSignalChanged)` · `Car(GearChanged)` | Pleos Vehicle SDK — `TurnSignal`, `Gear` |
| `Car(SpeedChanged)` | AAOS `CarPropertyManager` `PERF_VEHICLE_SPEED` (권한 없으면 안 넣어도 된다 — 레버+기어로만 판정) |
| `ClipEnded(index)` | 앱 플레이어 — 문장 하나를 끝까지 재생했을 때 |
| `PlayPressed` · `PausePressed` · `SkipPressed` | `MediaSession` 콜백(핸들 키·화면 버튼) |
| `FocusChanged` | `AudioManager` 포커스 콜백 |
| `Tick(at)` | `state.nextDeadline(params)` 시각에 한 번 — 상한·디바운스 판정 |

앱이 판단할 것은 없다. 「지금 멈출까」「언제 이어 갈까」는 전부 엔진의 `Effect`로 나온다.

## 2. 문장 경계는 어디서 오나

엔진은 「지금 문장이 끝났다」(`ClipEnded`)만 알면 된다. 문장 경계를 가진 콘텐츠는 이미 많다.

- **자막·대본이 있는 음성** — WebVTT·SRT 큐 하나를 문장 하나로 쓴다. `tools/pack_from_transcript.py <음성> <자막.vtt|srt> <out> <제목> <출처>`가 그대로 팩을 만든다(큐 경계로 자르기만, 내용 변경 없음).
- **대본이 없는 낭독 녹음** — word-level 받아쓰기의 문장 끝 부호로 경계를 찾는다(`tools/cut_pack.py`, 여러 장은 `tools/build_long_pack.py`).
- **스트리밍하는 앱** — 파일을 자를 필요 없이, 재생 위치가 다음 경계 시각을 지날 때 `ClipEnded`를 넣으면 된다.

## 3. 규칙 (요약 — 전문은 `docs/SPEC.md`)

| 규칙 | 내용 |
|---|---|
| R1 | 레버 ON + 기어 D + 움직이는 중 → 지금 문장 끝에서 멈출 준비(예약) |
| R3 | 신호 대기(0.3m/s 이하)에선 레버를 켜도 예약 안 함 |
| R4 | 예약 뒤 2.5초 안에 문장이 안 끝나면 그 자리에서 멈춤, 재개는 그 문장 처음부터 |
| R5·R8 | 재개 — 기본은 핸들 재생 키. 「자동 재개」를 켜면 레버 OFF 0.8초 유지 뒤 |
| R6 | 레버 한 번에 쉼표 한 번 |
| R10 | 다른 소리가 오디오 포커스를 가져가면 멈추고, 돌려주면 이어 간다(쉼표 중이면 그대로) |
| R11 | 후진(R) 진입도 주의 순간 — 레버 없이 지금 문장 끝에서 멈춤, R 구간 한 번 |

## 4. 확인 방법

`engine/src/test/.../IntegrationExampleTest.kt`가 이 문서의 1절 코드를 그대로 돌린다 — 가짜 플레이어 하나로
「읽는 중 깜빡이 ON → 문장 끝에서 다음 문장을 틀지 않고 멈춤 표시 → 핸들 키로 다음 문장」을 확인한다.
