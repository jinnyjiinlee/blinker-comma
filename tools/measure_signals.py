"""D2 신호 실측 — 에뮬레이터에서 신호를 하나씩 넣고 앱 로그(logcat)를 파일로 남긴다.

사용: python3 tools/measure_signals.py <로그 파일> [signals|cut3|bare]
  signals(기본) = 신호 하나씩 · cut3 = 「자동 재개 끔」 + 쉼표 중 핸들 키 · bare = 권한 실측(재생 없이 신호만)
  breath = 컷 1 모양(문장 끝 쉼표 → OFF 0.8초 → 다음 문장)
전제: 에뮬레이터(-no-audio)가 떠 있고 디버그 APK가 사용자 10에 설치돼 있음(속도는 pm grant --user 10).
  signals는 포커스를 가져갈 다른 앱이 필요하다 — FOCUS_PROBE=패키지/액티비티(앱이 `--es cmd focus_nav --ei hold_ms`를 받음).
손 = VC 웹 API(실제 신호 경로) · adb keyevent(핸들 키 대역 = 시연 주입) · 다른 앱의 포커스 요청 ·
디버그 주입 브로드캐스트. 단계마다 logcat에 `CommaMeasure` 표지 줄을 찍어 앱 로그와 한 파일에서 맞춰 본다.
"""

import re
import subprocess
import sys
import threading
import time
import urllib.request
from pathlib import Path

from device import APP, VC, adb, mark, screen_has
from measure_plans import PLANS

TAGS = [
    "Comma",
    "CommaSignal",
    "CommaPlayer",
    "CommaDeck",
    "CommaFocus",
    "CommaMedia",
    "CommaInject",
    "CommaPack",
    "CommaGlyph",
]


def keep_vc_active(stop: threading.Event) -> None:
    """VC는 브라우저 심장박동이 끊기면 쓰기를 막는다 — 1초마다 보낸다."""
    while not stop.is_set():
        request = urllib.request.Request(
            f"{VC}/browser/session?client=measure", method="POST"
        )
        urllib.request.urlopen(request, timeout=5).read()
        stop.wait(1.0)


def run_plan(plan: list, out: Path) -> None:
    """action None = 검사 줄: `CHECK screen 글자`(화면에 있나) · `WAIT 정규식`(로그에 찍힐 때까지)."""
    for name, action, wait_s in plan:
        kind, _, target = name.partition(" ")
        if action is None and kind == "WAIT":
            wait_for_line(out, target)
        elif action is None:
            mark(f"{name}: {screen_has(target.split(' ', 1)[1])}")
        else:
            mark(f"step {name}")
            action()
        time.sleep(wait_s)


def wait_for_line(out: Path, pattern: str, limit_s: float = 30.0) -> None:
    """대본 anchor와 같은 일 — 로그에 줄이 찍힐 때까지 기다린다(못 찾으면 멈춘다)."""
    deadline = time.monotonic() + limit_s
    while not re.search(pattern, out.read_text(encoding="utf-8")):
        if time.monotonic() > deadline:
            raise RuntimeError(f"로그에 「{pattern}」 없음")
        time.sleep(0.02)


def record(out: Path, plan: str) -> None:
    """logcat을 파일로 받으며 순서표를 돌린다."""
    filters = [f"{tag}:V" for tag in [*TAGS, "CommaMeasure"]] + [
        "AndroidRuntime:E",
        "*:S",
    ]
    with out.open("w", encoding="utf-8") as sink:
        logcat = subprocess.Popen(
            ["adb", "logcat", "-v", "time", *filters], stdout=sink
        )
        try:
            run_plan(PLANS[plan], out)
            time.sleep(1.0)
        finally:
            logcat.terminate()


def main(out: Path, plan: str) -> None:
    adb("forward", "tcp:8088", "tcp:8088")
    adb("shell", "am", "force-stop", "--user", "10", APP)
    adb("logcat", "-c")
    stop = threading.Event()
    threading.Thread(target=keep_vc_active, args=(stop,), daemon=True).start()
    try:
        record(out, plan)
    finally:
        stop.set()


if __name__ == "__main__":
    main(Path(sys.argv[1]), sys.argv[2] if len(sys.argv) > 2 else "signals")
