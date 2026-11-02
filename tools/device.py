"""에뮬레이터 손 — VC 웹 API(실제 신호 경로)·adb·화면 글자 누르기·디버그 주입. 실측 스크립트가 같이 쓴다."""

import os
import subprocess
import urllib.request

VC = "http://127.0.0.1:8088"
APP = "app.blinkercomma"
# 포커스를 가져갈 다른 앱(개발용 프로브 — 저장소 밖). 이름은 환경변수로만: FOCUS_PROBE=패키지/액티비티
PROBE = os.environ.get("FOCUS_PROBE", "")
INJECT = "app.blinkercomma.debug.INJECT"
SPEED_40 = "/vehicle/controls?speed=40&accelerator=0&brake=0"
SPEED_0 = "/vehicle/controls?speed=0&accelerator=0&brake=0"


def adb(*args: str) -> str:
    return subprocess.run(
        ["adb", *args], capture_output=True, text=True, check=True
    ).stdout


def vc_put(path: str) -> None:
    urllib.request.urlopen(
        urllib.request.Request(f"{VC}{path}", method="PUT"), timeout=5
    ).read()


def lever(mode: str) -> None:
    vc_put(f"/vehicle/turn-signal?mode={mode}")


def gear(name: str) -> None:
    vc_put(f"/vehicle/gear?gear={name}")


def mark(text: str) -> None:
    """logcat에 단계 표지 — 앱 로그와 같은 시계로 찍힌다."""
    adb("shell", "log", "-t", "CommaMeasure", f"'{text}'")


def screen_xml() -> str:
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    return adb("shell", "cat", "/sdcard/ui.xml")


def _label_at(text: str, xml: str) -> int:
    """글자(text) 또는 그림 버튼 이름(content-desc — 재생·정지는 그림이라 이름만 있음) 위치."""
    for key in ("text", "content-desc"):
        found = xml.find(f'{key}="{text}"')
        if found >= 0:
            return found
    raise ValueError(f"화면에 없음: {text}")


def tap_text(text: str, xml: str | None = None) -> None:
    xml = xml or screen_xml()
    bounds = (
        xml[_label_at(text, xml) :]
        .split('bounds="[', 1)[1]
        .split('"', 1)[0]
        .rstrip("]")
    )
    (left, top), (right, bottom) = (
        map(int, part.split(",")) for part in bounds.split("][")
    )
    adb("shell", "input", "tap", str((left + right) // 2), str((top + bottom) // 2))


def tap_if_shown(text: str) -> None:
    """글자가 화면에 있을 때만 누른다(팩이 끝나 이미 멈췄으면 「정지」가 없다). 덤프 한 번으로 보고 누른다."""
    xml = screen_xml()
    if screen_has(text, xml):
        tap_text(text, xml)


def screen_has(text: str, xml: str | None = None) -> bool:
    xml = xml or screen_xml()
    return f'text="{text}"' in xml or f'content-desc="{text}"' in xml


def launch_app() -> None:
    adb("shell", "am", "start", "--user", "10", "-n", f"{APP}/.MainActivity")


def media_key() -> None:
    """핸들 ▶❙❙ 대역(KEYCODE_MEDIA_PLAY_PAUSE) — 시연 주입."""
    adb("shell", "input", "keyevent", "85")


def inject(signal: str, value: str) -> None:
    extras = ["--es", "signal", signal, "--es", "value", value]
    broadcast = ["shell", "am", "broadcast", "--user", "10", "-a", INJECT, "-p", APP]
    adb(*broadcast, *extras)


def probe_focus(command: str) -> None:
    """다른 앱(개발용 프로브)이 오디오 포커스를 3초 가져간다 — 실제 포커스 경로. FOCUS_PROBE 없으면 멈춘다."""
    if not PROBE:
        raise RuntimeError(
            "FOCUS_PROBE=패키지/액티비티 환경변수 필요(포커스를 요청하는 다른 앱)"
        )
    extras = ["--es", "cmd", command, "--ei", "hold_ms", "3000"]
    start = ["shell", "am", "start", "--user", "10", "-n", PROBE]
    adb(*start, *extras)


def reset_car() -> None:
    vc_put(SPEED_0)
    gear("Park")
    lever("Off")
