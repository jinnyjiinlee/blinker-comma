#!/bin/bash
# 사용: scripts/check.sh <프로젝트 루트> [--release]
# ./gradlew check(ktlint·detekt 타입 분석·unit test·Kover·Lint) 위에 size·블라인드·Kmh·Python·stdlib·테스트 실행 수를 더한다
set -e
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="${1:-.}"
RELEASE_FLAG=""; [ "$2" = "--release" ] && RELEASE_FLAG="-Pcards.release=true"
# 블라인드 단어 목록은 저장소 밖(목록 자체가 실명) — 경로만 받는다
BLIND_WORDS="${BLIND_WORDS:-$HOME/.claude/skills/android-app/scripts/blind_words.txt}"
cd "$ROOT"
python3 "$HERE/check_size.py" .
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo '✗ git 저장소가 아님 — git init 먼저'; exit 1; }
if [ -f "$BLIND_WORDS" ]; then
    if git ls-files -co --exclude-standard -z | xargs -0 grep -nIiE -f "$BLIND_WORDS"; then
        echo '✗ 블라인드 위반 — 위 줄을 지운다'; exit 1
    fi
elif [ "$SKIP_BLIND" != "1" ]; then
    echo "✗ 블라인드 단어 목록 없음: $BLIND_WORDS (BLIND_WORDS=경로, 정말 건너뛰려면 SKIP_BLIND=1)"; exit 1
fi
# km/h는 화면에서만 — 엔진·카드는 m/s
if git ls-files -co --exclude-standard -- '*.kt' | grep -v '/ui/' | xargs grep -nE 'Kmh' 2>/dev/null; then
    echo '✗ km/h는 화면(ui/)에서만 — 엔진·카드는 m/s'; exit 1
fi
# set -e는 && 목록 안 실패로 멈추지 않는다 — ruff 실패가 「check OK」로 지나가던 구멍(10/25)
if [ -d tools ] && [ -d demo ]; then
    uvx ruff@0.17.2 check tools/ demo/ || { echo '✗ ruff check 실패'; exit 1; }
    uvx ruff@0.17.2 format --check tools/ demo/ || { echo '✗ ruff format — uvx ruff@0.17.2 format tools/ demo/'; exit 1; }
fi
# 화면 문자열은 strings.xml로 (Text("…") 리터럴 금지)
if grep -rnE 'Text\(\s*"' --include='*.kt' */src/main/kotlin 2>/dev/null; then
    echo '✗ 화면 문자열은 strings.xml로 — stringResource(R.string.x)'; exit 1
fi
# 라이브러리가 kotlin-stdlib를 몰래 올리면 실패
KOTLIN_VERSION=2.0.21
if ./gradlew -q :engine:dependencies --configuration runtimeClasspath \
    | grep -E 'kotlin-stdlib:[0-9.]+ -> ' | grep -v -- "-> ${KOTLIN_VERSION//./\\.}"; then
    echo "✗ 라이브러리가 kotlin-stdlib를 $KOTLIN_VERSION 위로 올림 — .pom 확인 후 낮은 버전으로"; exit 1
fi
./gradlew --quiet check $RELEASE_FLAG
python3 "$HERE/check_tests.py" .
echo "check OK"
