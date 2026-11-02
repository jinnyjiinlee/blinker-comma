"""실측 순서표 — 한 줄 = (단계 이름, 손, 기다릴 초). 순서·간격이 곧 실측 조건이라 데이터로 둔다."""

from device import (
    SPEED_0,
    SPEED_40,
    gear,
    inject,
    launch_app,
    lever,
    media_key,
    probe_focus,
    reset_car,
    tap_if_shown,
    tap_text,
    vc_put,
)

START = [
    ("reset P speed0 lever OFF", reset_car, 1.0),
    ("launch app", launch_app, 4.0),
]

SIGNALS = START + [
    ("vc gear Drive", lambda: gear("Drive"), 1.5),
    ("vc speed 40", lambda: vc_put(SPEED_40), 1.5),
    ("tap play", lambda: tap_text("▶"), 7.0),
    ("vc turn-signal BlinkRight (comma)", lambda: lever("BlinkRight"), 6.0),
    ("vc turn-signal Off (resume)", lambda: lever("Off"), 3.0),
    ("vc toggle 1.7s: BlinkRight", lambda: lever("BlinkRight"), 1.7),
    ("vc toggle 1.7s: Off", lambda: lever("Off"), 3.0),
    ("adb keyevent 85 while reading", media_key, 2.0),
    ("adb keyevent 85 while stopped", media_key, 3.0),
    ("probe app focus_nav 3s", lambda: probe_focus("focus_nav"), 6.0),
    ("back to app", launch_app, 2.0),
    ("inject focus LOSS_TRANSIENT", lambda: inject("focus", "LOSS_TRANSIENT"), 2.0),
    ("CHECK screen 시연 주입", None, 0.0),
    ("inject focus GAIN", lambda: inject("focus", "GAIN"), 2.0),
    ("vc speed 0", lambda: vc_put(SPEED_0), 1.5),
    ("vc turn-signal BlinkRight at speed 0", lambda: lever("BlinkRight"), 2.0),
    ("vc turn-signal Off", lambda: lever("Off"), 1.0),
    ("vc gear Park", lambda: gear("Park"), 1.5),
    ("tap pause (if still reading)", lambda: tap_if_shown("❙❙"), 1.5),
]

CUT3 = START + [
    ("tap auto resume off", lambda: tap_text("자동 재개 켬"), 1.0),
    ("vc gear Drive + speed 40", lambda: (gear("Drive"), vc_put(SPEED_40)), 1.5),
    ("tap play", lambda: tap_text("▶"), 3.0),
    ("vc turn-signal BlinkRight", lambda: lever("BlinkRight"), 6.0),
    ("vc turn-signal Off (expect keep hold)", lambda: lever("Off"), 2.0),
    ("adb keyevent 85 while holding", media_key, 3.0),
    ("tap auto resume on", lambda: tap_text("자동 재개 끔"), 1.0),
    ("tap pause", lambda: tap_text("❙❙"), 1.0),
]

BARE = START + [
    ("vc gear Drive", lambda: gear("Drive"), 1.5),
    ("vc speed 40", lambda: vc_put(SPEED_40), 1.5),
    ("vc turn-signal BlinkRight", lambda: lever("BlinkRight"), 1.5),
    ("vc turn-signal Off", lambda: lever("Off"), 1.5),
    ("vc gear Park", lambda: gear("Park"), 1.5),
]

# 컷 1 모양: 문장 2(3.0초)의 실제 소리 시작을 기다렸다가 끝 0.6초 전 레버 ON, 1.1초 뒤 OFF
BREATH = START + [
    ("vc gear Drive + speed 40", lambda: (gear("Drive"), vc_put(SPEED_40)), 1.5),
    ("tap play", lambda: tap_text("▶"), 0.0),
    ("WAIT CommaPlayer.*start s=2 ", None, 2.4),
    ("vc turn-signal BlinkRight (end-0.6)", lambda: lever("BlinkRight"), 1.7),
    ("vc turn-signal Off (+1.1 after end)", lambda: lever("Off"), 3.0),
    ("tap pause", lambda: tap_text("❙❙"), 1.0),
]

PLANS = {"signals": SIGNALS, "cut3": CUT3, "bare": BARE, "breath": BREATH}
