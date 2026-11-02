"""무대 지휘 — 대본(demo/script의 TSV) 한 컷을 Virtual Console·adb·화면 누르기로 그대로 재생한다.

사용: python3 demo/conductor.py demo/script/cut1_breath.tsv <로그 파일>
  전제: 에뮬레이터(-no-audio)에 앱이 떠 있고 P 화면. 컷 여러 개 + 새 설치·권한·녹화는 demo/rehearsal.py.
손: vc = VC 웹 API(실제 신호 경로) · app = P 화면 글자 누르기(녹화 전 준비) · adb = 핸들 키 대역(시연 주입) ·
  anchor = 로그에 그 줄이 새로 찍힐 때까지 기다렸다가 그 시각을 0으로(문장 시작 = 실제 소리 시작 CommaPlayer start).
"""

import subprocess
import sys
import threading
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "tools"))
from device import adb, gear, lever, media_key, screen_has, screen_xml, tap_text, vc_put
from log_watch import STAGE_TAG, LogWatch
from script_rows import ANCHOR_PATTERNS, Row, delay_after_anchor, read_rows, split_prep

GEARS = {"PARK": "Park", "DRIVE": "Drive", "REVERSE": "Reverse", "NEUTRAL": "Neutral"}
SIGNALS = {"RIGHT": "BlinkRight", "LEFT": "BlinkLeft", "OFF": "Off"}
TOGGLE_TEXT = {"ON": "켬", "OFF": "끔"}  # 앱 strings.xml toggle_state_on·off
# 준비 줄 사이 — 기어 콜백(D2 실측 5~8ms)이 닿을 시간. 마지막 줄 뒤엔 안 기다린다: ▶ 뒤 준비가 첫 문장 안에
# 끝나야 anchor(다음 문장 시작)가 늘 같은 문장에 걸린다(D4 리허설 1회차 — 1초×3이면 2.6초 문장을 넘겨 한 문장 밀림)
PREP_GAP_S = 0.5
VC = "http://127.0.0.1:8088"


def keep_vc_active(stop: threading.Event) -> None:
    """VC는 브라우저 심장박동이 끊기면 쓰기를 막는다 — 1초마다 보낸다."""
    import urllib.request

    while not stop.is_set():
        request = urllib.request.Request(
            f"{VC}/browser/session?client=conductor", method="POST"
        )
        urllib.request.urlopen(request, timeout=5).read()
        stop.wait(1.0)


def mark(text: str) -> None:
    """로그에 단계 표지(기다리지 않음) — 앱 로그와 같은 시계로 찍혀 컷 경계를 나눈다."""
    subprocess.Popen(["adb", "shell", "log", "-t", STAGE_TAG, f"'{text}'"])


def play_vc(signal: str, value: str) -> None:
    actions = {
        "gear": lambda: gear(GEARS[value]),
        "turn_signal": lambda: lever(SIGNALS[value]),
        "speed": lambda: vc_put(
            f"/vehicle/controls?speed={int(value)}&accelerator=0&brake=0"
        ),  # 대본 값 = km/h
    }
    actions[signal]()


def play_app(signal: str, value: str) -> None:
    """P 화면 누르기. 자동 재개는 지금 상태가 이미 원하는 쪽이면 누르지 않는다."""
    xml = screen_xml()
    if signal == "play":
        tap_text("재생", xml)
    elif signal == "auto_resume" and not screen_has(TOGGLE_TEXT[value], xml):
        tap_text(TOGGLE_TEXT["OFF" if value == "ON" else "ON"], xml)


def play_row(row: Row) -> None:
    if row.hand == "vc":
        play_vc(row.signal, row.value)
    elif row.hand == "app":
        play_app(row.signal, row.value)
    elif row.hand == "adb" and row.signal == "media_key":
        media_key()
    else:
        raise ValueError(f"모르는 손: {row}")


def run_cut(script: Path, watch: LogWatch) -> None:
    """준비 줄은 차례대로, anchor 뒤 줄은 anchor 시각 + at_s에. 줄마다 실제 시각을 찍는다."""
    prep, stage = split_prep(read_rows(script))
    mark(f"cut {script.stem} prep")
    for index, row in enumerate(prep):
        time.sleep(PREP_GAP_S if index else 0.0)
        play_row(row)
    anchor_at, remaining_s, since = 0.0, None, len(watch.lines)
    for row in stage:
        if row.is_anchor:
            line, found = watch.wait_for(ANCHOR_PATTERNS[row.value], since)
            anchor_at = watch.host_time(line)
            remaining_s = (
                (int(found[3]) - int(found[2])) / 1000
                if row.value == "clipStart"
                else None
            )
            mark(f"anchor {row.value.replace(' ', '_')} | {line.text}")
            print(f"anchor {row.value:10s} = {line.text}", flush=True)
        else:
            wait_and_play(row, anchor_at + delay_after_anchor(row.at, remaining_s))
        since = len(watch.lines)
    mark(f"cut {script.stem} end")


def wait_and_play(row: Row, due: float) -> None:
    time.sleep(max(0.0, due - time.time()))
    play_row(row)
    late_ms = (time.time() - due) * 1000
    mark(f"row {row.at} {row.hand} {row.signal} {row.value} late={late_ms:.0f}ms")
    print(
        f"  {row.at:8s} {row.hand:4s} {row.signal:12s} {row.value:10s} late={late_ms:4.0f}ms  {row.memo}",
        flush=True,
    )


def main(script: Path, log_path: Path) -> None:
    adb("forward", "tcp:8088", "tcp:8088")
    stop = threading.Event()
    threading.Thread(target=keep_vc_active, args=(stop,), daemon=True).start()
    watch = LogWatch(log_path)
    try:
        time.sleep(2.0)  # 심장박동이 먼저 닿아야 VC 쓰기가 열린다
        run_cut(script, watch)
        time.sleep(3.0)
    finally:
        watch.close()
        stop.set()


if __name__ == "__main__":
    main(Path(sys.argv[1]), Path(sys.argv[2]))
