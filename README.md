# 깜빡이 쉼표 (Blinker Comma)

깜빡이를 올리면 낭독이 그 문장 끝에서 숨을 참고, 레버가 돌아오면 다음 문장 — 화면도 손도 없이. Pleos Connect 차량용 앱.

## 지금 상태
- `:engine` 완료: 쉼표 예약 판정 · 낭독 상태 리듀서(쉼표 규칙 R1~R10) · 로그 줄 · 오늘 카운트 · 콘텐츠 팩 검사 · 시연 대본 3컷 리플레이
- `:app`: 신호 어댑터 3종(깜빡이·기어·속도) · 플레이어 · 낭독 서비스(MediaBrowserService + MediaSession + 포그라운드) · 오디오 포커스 · 화면 3개(P 정차 · D 주행 · 권한 안내 — 어두운 테마, 측후방 카메라 자리 피함) · 런처 아이콘 · 디버그 빌드 시연 주입
- 콘텐츠: 지금 팩은 **측정용 합성음**(`tools/make_probe_pack.sh`) — 퍼블릭 도메인 낭독 팩으로 교체 예정
- 신호 실측: `docs/signals_measured.md`(에뮬레이터, 이 앱 기준)
- 테스트 수: `scripts/check.sh .`의 `tests OK (…)` 숫자 — 현재 `app 92, engine 116` (app = 공통 테스트 42개 × debug·release + 디버그 전용 8개)

## 처음 실행
```
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # JDK 21 (25는 Gradle 8.13과 안 맞음)
git config core.hooksPath config/git-hooks          # 클론 후 1회 — fix: 커밋은 테스트와 같이
./gradlew check                                       # ktlint·detekt(타입 분석)·테스트·커버리지·Lint
scripts/check.sh .                                    # + 파일 크기·블라인드·테스트 실행 수
./gradlew :app:assembleDebug
```

## 먼저 읽을 것
1. docs/SPEC.md — 원칙 11개 · 쉼표 규칙 R1~R10
2. docs/ARCHITECTURE.md — 구조·용어 사전(이 단어만 씀)·대본 형식
3. CONVENTIONS.md — 숫자 상한·공통 부품·테스트 대장

## 시연 주입 신호
- 핸들 미디어 키: Virtual Console에 카드가 없어 `adb shell input keyevent 85`로 넣는다(컷 3, 영상 밖). 대본 `demo/script/cut3_key_resume.tsv`의 `adb` 줄.
- 그 밖에 VC로 못 넣는 신호(오디오 포커스 등): 디버그 빌드에서만 `adb shell am broadcast --user 10 -a app.blinkercomma.debug.INJECT -p app.blinkercomma --es signal focus --es value LOSS_TRANSIENT` — 화면에 「시연 주입」이 뜬다. 값 목록은 `docs/signals_measured.md` 끝.
- 에뮬레이터 권한: 앱은 사용자 10에서 돈다 — 속도는 `adb shell pm grant --user 10 app.blinkercomma android.car.permission.CAR_SPEED`.

## 꼭 지킬 것
- 블라인드 심사: 커밋 작성자·코드·주석·문서에 실명·회사명·로컬 경로·전화번호 금지
- 키스토어·비밀값은 저장소 밖
