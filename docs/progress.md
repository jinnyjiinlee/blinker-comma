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
