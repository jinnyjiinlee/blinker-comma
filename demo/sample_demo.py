"""쉼표 샘플(:sample) 시연 녹화 — 같은 엔진을 쓰는 두 번째 앱(음원 하나 + 자막 경계)에서 깜빡이 쉼표가 똑같이 동작하는지.

사용: python3 demo/sample_demo.py <녹화 mp4> <로그 파일>
  전제: 에뮬레이터(-no-audio)에 sample-debug.apk 설치. 샘플은 자동 재개 켬(레버 OFF 0.8초 뒤 다음 자막).
"""

import subprocess
import sys
import threading
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "tools"))
from conductor import keep_vc_active
from device import adb, gear, lever, reset_car, tap_text, vc_put
from log_watch import LogWatch

SAMPLE = "app.blinkercomma.sample/.SampleActivity"
DEVICE_VIDEO = "/sdcard/sample.mp4"
CUE2_S = 5.52  # 두 번째 자막 길이(assets/episode.vtt) — 끝나기 1초 전에 레버


def scene(watch: LogWatch) -> None:
    tap_text("재생")
    gear("DRIVE")
    vc_put("/vehicle/controls?speed=40&accelerator=0&brake=0")
    watch.wait_for("clipStart s=2", 0, limit_s=20)
    time.sleep(CUE2_S - 1.0)
    lever("BlinkRight")
    watch.wait_for("→ hold", 0, limit_s=10)
    time.sleep(1.2)
    lever("Off")
    watch.wait_for("OFF held", 0, limit_s=10)
    time.sleep(3.0)


def main(video: Path, log_path: Path) -> None:
    stop = threading.Event()
    threading.Thread(target=keep_vc_active, args=(stop,), daemon=True).start()
    time.sleep(2.0)
    reset_car()
    adb(
        "shell", "am", "force-stop", "--user", "10", "app.blinkercomma"
    )  # 본 앱 재생이 로그에 섞이지 않게
    adb("shell", "am", "force-stop", "--user", "10", SAMPLE.split("/")[0])
    adb("shell", "am", "start", "--user", "10", "-n", SAMPLE)
    time.sleep(4.0)
    adb("logcat", "-c")
    watch = LogWatch(log_path)
    recorder = subprocess.Popen(
        ["adb", "shell", "screenrecord", "--time-limit", "60", DEVICE_VIDEO]
    )
    try:
        time.sleep(1.5)
        scene(watch)
    finally:
        reset_car()
        time.sleep(1.0)
        adb("shell", "pkill", "-INT", "screenrecord")
        recorder.wait(timeout=20)
        watch.close()
        stop.set()
    time.sleep(1.0)
    adb("pull", DEVICE_VIDEO, str(video))


if __name__ == "__main__":
    main(Path(sys.argv[1]), Path(sys.argv[2]))
