# 깜빡이 쉼표 (Blinker Comma)

## 1. 앱 이름 · 한 줄 소개
- **앱 이름**: 깜빡이 쉼표 (패키지 `app.blinkercomma`, 출품 이름 CommaLane — 개인 참가이며 블라인드 심사용 이름. APK 파일명 규정 「팀명_앱이름_release.apk」의 팀명 자리에 쓴다)
- **한 줄 소개**: 차로를 바꾸거나 후진하는 순간, 차 안 낭독이 듣던 문장 끝에서 숨을 참았다가 핸들 버튼 한 번에 다음 문장부터 이어 준다 — 그리고 이 판단을 다른 오디오 앱에 붙이는 「쉼표 규칙」 엔진.
- 깜빡이(달리는 중)나 후진 기어가 들어오면 지금 문장을 끝까지 읽고 멈춘다(⏸). 핸들 재생 버튼을 누르면 다음 문장부터. 「자동 재개」(기본값 끔)를 켜 두면 레버가 0.8초 내려가 있을 때 버튼 없이 이어 읽는다. 시동을 껐다 켜도 마지막 문장부터 이어 듣는다. 차량 제어 0 · 주행 중 화면 조작 0 · 네트워크·계정 0.
- **두 번째 앱 `sample/`(쉼표 샘플)**: 엔진만 가져다 쓴 팟캐스트형 플레이어 — 음원 파일 하나 + WebVTT 자막 경계(`CommaDriver.kt` 연동, `CuePlayer.kt` 재생). `./gradlew :sample:assembleDebug`, 시연 녹화 `python3 demo/sample_demo.py <mp4> <로그>`(로그 `docs/logs/sample_demo_logcat.txt`).
- **다른 오디오 앱에 붙이기**: 판단은 전부 순수 Kotlin `engine` 모듈에 있다 — `docs/INTEGRATION.md`(연동 방법·규칙 요약), `IntegrationExampleTest`(그대로 돌아가는 예제), `tools/pack_from_transcript.py`(자막 WebVTT·SRT가 있는 음성 → 팩).

## 2. 실행 방법
```
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # JDK 21 (25는 Gradle 8.13과 안 맞음)
./gradlew :app:assembleDebug                          # app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start --user 10 -n app.blinkercomma/.MainActivity   # 에뮬레이터는 사용자 10에서 앱이 돈다
```
1. 첫 실행(정차 중)에 권한 안내가 한 번 뜬다 → [허용] → 시스템 창 「허용」(차 속도 권한이 「위치 정보」로 보인다). 거부해도 깜빡이·기어만으로 동작한다.
2. P 화면에서 ▶ → 낭독 시작(앱이 스스로 재생을 시작하지 않는다).
3. Virtual Console에서 기어 D · 속도 40 → 화면이 어두운 빈 화면(D)으로 바뀐다.
4. 깜빡이 BlinkRight → 그 문장이 끝나는 순간 멈추고 ⏸ → 핸들 재생 버튼(`adb shell input keyevent 85`)으로 다음 문장. P 화면에서 「자동 재개」를 켜 두었다면 깜빡이 Off → 0.8초 뒤 다음 문장.

**시연 대본 자동 재생**(Virtual Console 웹 API + adb):
```
adb forward tcp:8088 tcp:8088
python3 demo/conductor.py demo/script/cut1_breath.tsv cut1_log.txt          # 한 컷(앱이 P 화면에 떠 있을 때)
python3 demo/rehearsal.py <APK> rehearsal.mp4 rehearsal_log.txt             # 새 설치 → 권한 → 3컷 + 화면 녹화
python3 demo/rehearsal_summary.py rehearsal_log_1.txt rehearsal_log_2.txt   # 두 번 돌린 결과가 같은지
```
대본(`demo/script`의 TSV, 형식은 `docs/ARCHITECTURE.md` 6절): 컷 1 「숨 참기」 · 컷 2 「신호 대기 → 출발」 · 컷 3 「자동 재개 끔 + 핸들 키」 · 컷 4 「후진」. 리허설은 디버그 APK로 돈다(릴리스는 속도 로그를 남기지 않아 대본이 속도 반영을 확인할 수 없다).

**영상에 소리 입히기**: 에뮬레이터는 `-no-audio`라 녹화에 소리가 없다. `python3 demo/soundtrack.py <녹화.mp4> <로그> <팩 폴더> <영상 0초의 로그 시각> <출력.mp4>`가 앱 로그의 재생 시작·끝 시각대로 팩 음원을 붙인다.

**시연 주입(정직 표기)**: 핸들 미디어 키는 Virtual Console에 카드가 없어 `adb shell input keyevent 85`로 넣는다(컷 3). 그 밖에 VC로 못 넣는 신호(오디오 포커스 등)는 디버그 빌드에서만 `adb shell am broadcast --user 10 -a app.blinkercomma.debug.INJECT -p app.blinkercomma --es signal focus --es value LOSS_TRANSIENT`로 넣고, 그동안 화면에 노란 「시연 주입」이 뜬다(릴리스 APK엔 수신기 없음).

## 3. 테스트 환경 · 설정
| 항목 | 값 |
|---|---|
| 에뮬레이터 | Pleos Connect 에뮬레이터 2.1.0 · Android API 34 · 화면 2560×1440(앱 창 1682×1210dp) · `-no-audio`로 실행 |
| SDK | Pleos Vehicle SDK 2.3.2(깜빡이·기어) + AAOS `CarPropertyManager`(차속) · MediaBrowserService + MediaSession |
| 최소 / 대상 | minSdk 28 · Kotlin 2.0.21 · Gradle 8.13 · JDK 21 |
| 권한 | `pleos.car.permission.CAR_LIGHTS`(깜빡이) · `CAR_DRIVE`(기어) · `android.car.permission.CAR_SPEED`(런타임 — 에뮬레이터 수동 허용은 `adb shell pm grant --user 10 app.blinkercomma android.car.permission.CAR_SPEED`) · 포그라운드 서비스(미디어 재생) · 알림 |
| 신호 실측 | `docs/signals_measured.md` — 이 앱 기준 에뮬레이터 실측(깜빡이·기어·속도·핸들 키·오디오 포커스) |
| 검사 | `scripts/check.sh .` = ktlint · detekt(타입 분석) · 단위·시나리오 테스트 · 커버리지 · Lint · 파일 크기 · ruff → 현재 `tests OK (app 118, engine 127, sample 4)` |
| 리허설 | 새 설치 → 권한 → 4컷 자동 재생을 연속 2회 돌려 앱 로그 줄·시각이 같은지 확인 — `docs/progress.md` P3 |

- 소리: 에뮬레이터를 `-no-audio`로 돌리므로 소리는 녹음되지 않는다. 문장 끝은 재생 완료 콜백이 아니라 콘텐츠 팩의 문장 길이 시계로 판정한다(콜백이 에뮬레이터에서 일찍·늦게 와서 — 실측 근거는 `docs/signals_measured.md`).
- 콘텐츠: **사람이 읽은 퍼블릭 도메인 낭독 2팩** — 한국어 현진건 단편 약 68분(1,326조각, `packs/hyun`, 기본) · 영어 이솝 우화 약 62분(1,066조각, `packs/aesop`). 정차 화면의 팩 제목을 누르면 바뀐다(앱이 새 팩으로 다시 시작). LibriVox, Public Domain Mark — 각 폴더 `CREDITS.txt`. `tools/build_long_pack.py`가 받아쓰기 문장 끝으로 자르고, 긴 문장은 원문 쉼표·받아쓰기 구간 경계에서 나누며(`pack_split.py`), 자르는 자리는 가장 조용한 순간(`quiet_cut.py`). 예전 3분 팩 `packs/en1`은 그대로 둔다.

## 4. APK 위치
- 내려받기 주소로 받은 경우: 소스 zip에는 용량(Vercel 파일당 100MB) 때문에 `apk/` 폴더를 빼고, 같은 파일을 내려받기 페이지 ②번(`CommaLane_blinker-comma_release.apk`)으로 따로 두었다. 두 파일의 SHA-256은 내려받기 페이지에 있다.
- 제출용 릴리스 APK: **`apk/CommaLane_깜빡이쉼표_release.apk`**(서명 완료 — `apksigner verify` v2 스킴 통과, 2026-11-14 재확인). 빌드: `./gradlew :app:assembleRelease`(서명은 저장소 밖 `~/.blinker-comma/keystore.properties`를 읽음 — 그 파일이 없으면 서명 없이 빌드됨).
- 지금 빌드되는 디버그 APK: `./gradlew :app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`(저장소엔 올리지 않음).

## 개발자용
- 먼저 읽을 것: `docs/SPEC.md`(원칙 11개 · 쉼표 규칙 R1~R11) → `docs/ARCHITECTURE.md`(구조 · 용어 사전 · 대본 형식) → `CONVENTIONS.md`(숫자 상한 · 공통 부품 · 테스트 대장).
- 클론 후 1회: `git config core.hooksPath config/git-hooks`(`fix:` 커밋은 테스트와 같이).
- 블라인드 심사: 커밋 작성자·코드·주석·문서에 실명·회사명·로컬 경로·연락처 금지. 키스토어·비밀값은 저장소 밖.
