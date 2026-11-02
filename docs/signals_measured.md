# 신호 실측 — 이 앱 기준 (D2, 2026-11-02)

환경: Pleos Connect 에뮬레이터 2.1.0 · API 34 · `-no-audio -no-window` · 앱 사용자 10 · 디버그 APK.
방법: `tools/measure_signals.py <로그> <순서표>` — 순서표(`tools/measure_plans.py`)대로 VC 웹 API·adb를 누르고, 단계마다 logcat에 `CommaMeasure` 표지 줄을 찍어 앱 로그와 같은 파일·같은 시계로 남긴다.
지연 = 표지 줄(VC 요청 직전) → 앱 콜백 줄. 숫자는 아래 로그 파일에 있는 것만.
포커스 장면은 포커스를 요청하는 다른 앱(개발용 프로브 — 저장소 밖)이 필요해 `FOCUS_PROBE=패키지/액티비티`로 넘긴다.

| 로그 파일(`docs/logs/`) | 순서표 | 무엇 |
|---|---|---|
| `d2_signals_logcat.txt` | signals | 신호 하나씩 — 기어·속도·깜빡이·1.7초 토글·핸들 키·포커스(다른 앱·주입) |
| `d2_breath_logcat.txt` · `d2_breath2_logcat.txt` | breath | 컷 1 모양 2회 — 문장 끝 쉼표 → OFF → 0.8초 → 다음 문장 |
| `d2_cut3_logcat.txt` | cut3 | 「자동 재개 끔」 + 쉼표 중 핸들 키 |
| `d2_perm_none_logcat.txt` | bare | Pleos 권한 0개 + 속도 권한 거부(새로 설치) |
| `d2_perm_lights_only_logcat.txt` | bare | Pleos 권한 CAR_LIGHTS만 |

## 신호별 결과

| 신호 | 넣은 손 | 판정 | 실측(근거 줄) |
|---|---|---|---|
| 깜빡이 RIGHT·OFF | VC `PUT /vehicle/turn-signal?mode=BlinkRight·Off` | ✅ | 지연 5~7ms — signals `23:44:39.515 → .522`(RIGHT) · `23:44:45.547 → .552`(OFF). LEFT는 VC로 안 넣어 봄(엔진은 LEFT·RIGHT 같은 뜻) |
| 깜빡이 1.7초 간격 토글 | VC | ✅ 콜백 2개 다 옴 | signals `23:44:48.592` RIGHT · `23:44:50.330` OFF(간격 1.738초) → `comma cancelled`(R2). 설계서 위험 「1.7초 간격 콜백 2개」 닫힘 |
| 기어 P·D | VC `PUT /vehicle/gear?gear=Drive·Park` | ✅ | 지연 7~8ms — signals `23:44:27.248 → .256`(D) · `23:45:17.635 → .642`(P) |
| 속도 0·40km/h | VC `PUT /vehicle/controls?speed=` | ✅ | 40 → `speedMps=11.11` · 지연 178ms(`23:44:28.782 → .960`), 0 → 141ms. 이전 실행에선 7~455ms — 연속 속성이라 갱신 주기에 따라 흔들림 |
| 속도 0에서 레버 | VC | ✅ | signals `23:45:14.558 lever=ON speed=0 → no comma`(R3) |
| 첫 값(등록 직후) | — | ⚠️ | 등록하면 현재 값이 곧바로 2번씩 온다(깜빡이 OFF×2·기어×2). 직전 실행의 값이 먼저 오는 때가 있다 — breath2 `23:42:34.234 gear=DRIVE` → `.243 gear=PARK`(VC는 실행 전 P로 맞춤). 엔진은 정지 상태라 영향 없음 · 대본은 앱을 띄운 뒤 기어를 다시 넣는다 |
| 핸들 미디어 키 | `adb shell input keyevent 85` = **시연 주입**(VC 카드 없음) | ✅ | 우리 MediaSession이 받음(`media button code=85`) · 재생 중 → `mediaKey PAUSE → stop` · 정지 중 → `mediaKey PLAY → play` · 쉼표 중(자동 재개 끔) → `mediaKey PLAY → play`(cut3 `23:41:56.971`, R9). 키 → 콜백 약 0.3초(androidx 세션이 더블탭을 기다림 — signals `23:44:55.476 → .778`) |
| 오디오 포커스 — 다른 앱 | 개발용 프로브 앱이 내비 안내 용도로 3초 요청(실제 포커스 경로) | ✅ | `focus change=-2` → `focus=LOSS_TRANSIENT` · 멈춘 자리 2805ms → 3초 뒤 `change=1` → `focus=GAIN` → `start s=5 fromMs=2805`(signals `23:44:58.596~23:45:01.607`, R10) |
| 오디오 포커스 — 주입 | 디버그 브로드캐스트(아래) = **시연 주입** | ✅ | `demo inject focus=LOSS_TRANSIENT` → 멈춤, `GAIN` → 이어 재생 · 화면 「시연 주입」 표시 확인(`CHECK screen 시연 주입: True`) |
| 클립 재생 완료 콜백 | MediaPlayer | ❌ 믿을 수 없음(에뮬레이터) | 늦게도: 4.0초 클립에 4.85초(D2 1차 실행, 설계 바꾸기 전 — 로그 덮어씀) · 이르게도: 3.0초 클립에 2.36초(breath 이전 실행)·2.23초(`d2_breath2` `callback s=2 playedMs=2231`). 일시정지해도 콜백이 옴(1차 실행). → **문장 끝은 팩 길이 시계로**, 콜백은 기록만(아래 「플레이어」) |
| VC로 못 넣는 것 | — | VC 조작 불가 | 핸들 키(위 adb) · 오디오 포커스(위 다른 앱/주입) · 운전자 ▶(P 화면 누름 — 녹화 전 준비) |

## 권한 최소 집합

| 매니페스트 | 깜빡이 | 기어 | 근거 |
|---|---|---|---|
| Pleos 권한 0개 | ❌ `SecurityException: registerTurnSignalState need Permission` | ❌ `registerGearSelection need Permission` | `d2_perm_none_logcat.txt` 4·39행 |
| `CAR_LIGHTS`만 | ✅ | ❌ | `d2_perm_lights_only_logcat.txt` 4행(기어만 실패) · 깜빡이 RIGHT·OFF 수신 |
| `CAR_LIGHTS` + `CAR_DRIVE` | ✅ | ✅ | signals |

- → 최소 집합 = `pleos.car.permission.CAR_LIGHTS`(깜빡이) + `CAR_DRIVE`(기어) + `android.car.permission.CAR_SPEED`(속도, 런타임). Pleos 둘은 설치 때 허용됨(`granted=true`). SDK가 스스로 넣는 건 `VEHICLE_BINDING` 하나뿐(AAR 매니페스트).
- 속도 권한 거부: AAOS 등록이 **예외 없이 조용히 아무것도 안 줬다**(첫 시도 — 로그에 아무 줄 없음) → 권한을 먼저 보고 없으면 흐름을 닫게 고침 → `speed 확인 안 됨 — 구독 실패 … CAR_SPEED not granted`(`d2_perm_none` 74행). 엔진은 속도 null = 「확인 안 됨」으로 레버 + D만 본다(원칙 4).

## 컷 1 모양 (breath, 2회 — 문장 2의 실제 소리 시작 = 0)

| 줄 | 1회 | 2회 | 대본 기대(합성 팩 2.6초 기준) |
|---|---|---|---|
| ⓐ `turnSignal=RIGHT` · `moving → comma armed` | 2.45 | 2.47 | 끝 − 0.6 |
| ⓑ `clipEnd s=2 lever=ON → hold` · `holdGlyph=on` | 3.01 | 3.00 | 끝(3.0초 클립) |
| ⓒ `turnSignal=OFF` | 4.19 | 4.22 | 끝 + 1.1 (측정 손이 uiautomator 없이 VC만이라 +1.19·+1.22) |
| ⓓ `OFF held 0.80s → play s=3` | 5.00 | 5.02 | ⓒ + 0.80 ✅ |
| ⏸ 꺼짐(`holdGlyph=off`) = 문장 3 소리 시작 | +0.004 | +0.003 | ⓓ와 같은 순간 |

- ⓓ − ⓒ = 0.80초(로그 글자 그대로) — `nextDeadline` 1회 깨우기라 폴링 흔들림 없음.
- 실제 시연은 문장 A를 2.6초짜리로 두면 ⓐ 2.0 · ⓑ 2.6 · ⓒ 3.7 · ⓓ 4.5가 같은 간격으로 나온다(대본 계산 — 녹화로 확인은 D4).

## 플레이어 — 에뮬레이터 `-no-audio`에서 본 것과 대응

| 본 것 | 근거 | 대응(코드) |
|---|---|---|
| 완료 콜백이 늦거나 이르거나 안 옴 | 위 표 | 문장 끝 = 팩 `durationMs` 시계(`ClipPlayer`), 콜백은 `completion callback … playedMs=` 기록만. 끝 로그 `end s=N by=clock playedMs=…`(signals 8문장 모두 playedMs = 남은 길이 +0~18ms) |
| 플레이어 하나를 일시정지·재사용하면 다음 `start`가 5~6초 막힘 | D2 1차 실행(설계 바꾸기 전 — 로그 덮어씀) | 재생마다 새 플레이어, 멈춤은 「놓고 이어 들을 자리 기억」 |
| `prepare` 1.3~7.2초 · `release` 최대 7.6초 · 옛 플레이어에 `setVolume` 한 번이 8.5초 막힘 | 중간 실행들(`CommaDeck` 줄, 덮어씀) — 마지막 판에선 아래 대응으로 0 | MediaPlayer는 전용 스레드(`ClipDeck`), 놓기는 또 다른 스레드, 옛 플레이어엔 release 말고 아무것도 안 부름. **다음 클립을 미리 준비**(재생 시작 때 다음 문장, 멈출 때 그 문장, 서비스 시작 때 첫 문장) → 최종 4개 로그(signals·cut3·breath·breath2)의 열기 25번 전부 `warm=true prepareMs=0` · 시작 0~94ms |
| 준비가 막힌 사이 멈춤이 오면 늦게 끝난 준비가 소리를 냄(유령 재생) | signals 중간 실행 `open prepareMs=5218` 뒤 `start` | 시작 직전 표(ticket) 재확인 → `open dropped — cancelled while preparing` |
| 열기가 늦으면 ⏸가 소리보다 먼저 꺼짐 | 설계 원칙 「소리가 다시 날 때 끈다」 | 재개 때 ⏸ 끄기를 플레이어의 실제 시작까지 미룸(`holdGlyph=off`가 `CommaPlayer start`와 같은 ms) |

- 남은 위험: 미리 준비가 아직 안 끝났을 때 재생하면 준비 시간을 그대로 기다린다(signals `start s=5 … openMs=1008` — 준비 스레드가 앞 일을 하던 중). 대본은 첫 ▶를 녹화 전에 누르고, anchor는 엔진 `clipStart`가 아니라 **`CommaPlayer start s=N`(실제 소리 시작)**에 건다(D4 conductor).
- ⚠️ 앱 실행 직후 메인 스레드가 약 4초 늦은 적 1번(4번 실행 중) — breath `23:42:10.378` 속도 첫 값 뒤 깜빡이·기어 콜백(`at`은 24.05초)이 `23:42:14.475`에야 처리됨. 원인 확인 안 됨(실행 직후 첫 문장 미리 준비와 오디오 서비스가 겹친 것으로 짐작). 시각 `at`은 콜백 순간 값이라 판정은 맞고, 대본은 앱을 녹화 전에 띄워 둔다.
- 실차에서 완료 콜백과 시계 차이는 「확인 안 됨」 — `completion callback` 줄로 잴 수 있게 남겨 둠.

## 시연 주입(디버그 빌드 전용)

```
adb shell am broadcast --user 10 -a app.blinkercomma.debug.INJECT -p app.blinkercomma \
  --es signal <turn_signal|gear|speed_mps|focus|media_key> --es value <값>
```
- 값: `turn_signal` OFF·LEFT·RIGHT · `gear` PARK·REVERSE·NEUTRAL·DRIVE · `speed_mps` m/s 숫자 · `focus` GAIN·LOSS·LOSS_TRANSIENT · `media_key` PLAY·PAUSE·NEXT·PREVIOUS.
- 수신기는 `src/debug`에만 있고 `android.permission.DUMP`로 막아 adb 셸만 보낼 수 있다. 릴리스 APK엔 없음.
- 넣은 순간부터 화면에 노란 「시연 주입」, 로그 `CommaInject demo inject …`.
- 실제로 쓸 곳: 오디오 포커스 장면(R10)을 다른 앱 없이 보여줄 때. 깜빡이·기어·속도는 VC가 되므로 주입하지 않는다.
