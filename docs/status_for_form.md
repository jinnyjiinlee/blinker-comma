# 기능 구현 현황 (제출 서류용) — D5

기준: 2026-11-02. 「구현 완료(에뮬레이터 실측)」는 Pleos Connect 에뮬레이터(2.1.0·API 34·`-no-audio`)에서 실제 신호 경로로 확인한 것만 표기한다. 근거 로그 경로는 각 행에 적는다.

## 핵심 기능

| 기능 | 상태 | 근거 |
|---|---|---|
| 깜빡이 신호로 문장 끝에서 멈춤(쉼표, R1) | 구현 완료(에뮬레이터 실측) | `docs/logs/d5_rehearsal_logcat.txt`, `docs/progress.md` D2·D4 |
| 레버가 문장 끝 전에 내려오면 쉼표 취소(R2) | 구현 완료(에뮬레이터 실측) | `engine` 테스트 `EdgeScenarioTest` |
| 속도 0에서 레버를 켜면 예약 안 함, 출발 시 예약(R3) | 구현 완료(에뮬레이터 실측) | `docs/logs/d5_rehearsal_logcat.txt`(컷 2) |
| 예약 뒤 2.5초 안에 안 끝나면 문장 중간에서 멈춤, 재개 시 처음부터(R4) | 구현 완료(엔진 테스트) | `ReaderEndWaitTest` |
| 레버 OFF 0.8초 유지 뒤 다음 문장 재개(R5) | 구현 완료(에뮬레이터 실측) | `docs/logs/d5_rehearsal_logcat.txt`(컷 1: OFF→0.80s→재생) |
| 레버 한 번당 쉼표 최대 1회(R6) | 구현 완료(엔진 테스트) | `ReaderArmTest`, `ReadingTallyTest` |
| 멈춘 동안 정차·기어 변경에도 멈춤 유지(R7) | 구현 완료(엔진 테스트) | `ReaderResumeTest` |
| 「자동 재개 끔」 모드 — 핸들 키로만 재개(R8) | 구현 완료(에뮬레이터 실측) | `docs/logs/d5_rehearsal_logcat.txt`(컷 3) |
| 핸들 재생 버튼으로 즉시 재개(R9) | **시연 주입**(입력만) — 반응은 실측 | Virtual Console에 핸들 키 카드가 없어 `adb shell input keyevent 85`로 넣음(OS MediaSession 표준 경로, 앱은 실제 콜백 수신). `docs/signals_measured.md`, `docs/logs/d2_cut3_logcat.txt` |
| 다른 앱이 오디오 포커스를 가져가면 멈추고 돌려주면 재개(R10) | 구현 완료(에뮬레이터 실측) | 다른 앱의 실제 포커스 요청으로 확인. `docs/signals_measured.md` 「오디오 포커스」 |
| 팩 마지막 문장이 레버 ON인 채 끝나도 쉼표가 걸림(경계 케이스) | 구현 완료(엔진 테스트, D5 결함 수정) | `ReaderClipTest`, `docs/qa_M1.md` 결함 3 |
| P 화면(정차 — 표지·사용법·오늘 카운트·자동 재개 토글) | 구현 완료(에뮬레이터 실측) | `setup/build_shots/d5_01_park_realpack.png`, `docs/progress.md` D3 |
| D 화면(주행 — 쉼표 중 ⏸ 하나, 그 외 글자·터치 0) | 구현 완료(에뮬레이터 실측) | `docs/qa_M1.md` SPEC 원칙 1 확인 |
| 첫 실행 속도 권한 안내(정차에서만, 1회) | 구현 완료(에뮬레이터 실측) | `docs/progress.md` D3 |
| 오늘 문장 수·쉼표 수 실제 카운트 | 구현 완료(에뮬레이터 실측) | `setup/build_shots/d5_01_park_realpack.png`(16문장·3쉼표 — 전 세션 누적), `docs/qa_M1.md` 원칙 4 |
| 콘텐츠 팩 — 퍼블릭 도메인 영어 낭독 38문장 | 구현 완료(D5) | LibriVox "The Aesop for Children"(Public Domain Mark 1.0). `app/src/main/assets/packs/en1/CREDITS.txt` |
| 시연 대본 3컷 자동 재생(Virtual Console 연동) | 구현 완료(에뮬레이터 실측, 디버그·릴리스 APK 둘 다 확인) | `docs/logs/d5_rehearsal_logcat.txt`, `docs/logs/d5_release_rehearsal_logcat.txt` |
| 릴리스 서명 APK | 구현 완료(D5) | `apk/CommaLane_깜빡이쉼표_release.apk` — `apksigner verify` v2 스킴 통과 |

## 개발 계획 (MVP 범위 밖 — 기획서 「개발 계획」란)

| 기능 | 비고 |
|---|---|
| 외부 앱 모드(윌라·멜론 등 다른 앱 소리를 레버로 제어) | 기술 검토 결과 2단계로 결정(`docs/SPEC.md` 「뺀 것」) |
| 구독사(윌라 등) 연동·문장 경계 메타 수신 | — |
| 트리거 확장(급제동·후진·뒷문·좌석) | — |
| 콘텐츠 팩 여러 개·다운로드 | 지금은 앱 내장 팩 1개 |
| 배속 조절 | — |
| 앱 재시작 뒤 이어듣기 | 지금은 재시작 시 처음부터 |
| 영어 UI(다국어) | 지금은 한국어 화면만 |
| 낭독 콘텐츠 직접 녹음 | 지금은 퍼블릭 도메인 녹음 재사용(출처 표기) — 녹음자가 정해지면 팩만 교체 |

## 참고

- 전 항목의 세부 실측 수치·로그 경로는 `docs/progress.md`(D1~D5)·`docs/signals_measured.md`·`docs/qa_M1.md`에 있다.
- 「시연 주입」은 공모전 규정상 Virtual Console로 못 넣는 신호(핸들 키 입력)에 한해 쓰며, 영상·화면·로그에 그대로 표기한다(앱 쪽 반응은 실제 신호 경로와 동일).
