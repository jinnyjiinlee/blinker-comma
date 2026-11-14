"""리허설 한 번 — 새 설치 → 권한 → 대본 3컷 자동 재생을 화면 녹화(adb screenrecord)와 앱 로그로 남긴다.

사용: python3 demo/rehearsal.py <APK> <녹화 mp4> <로그 파일>
  전제: 에뮬레이터(-no-audio)가 떠 있음. 컷 사이엔 앱을 다시 띄워 매 컷 첫 문장부터(팩 끝에 닿지 않게).
비교: python3 demo/rehearsal_summary.py <로그 1> <로그 2>
"""

import subprocess
import sys
import threading
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "tools"))
from conductor import keep_vc_active, mark, run_cut
from device import APP, adb, launch_app, reset_car, screen_has, screen_xml, tap_text
from log_watch import LogWatch

SCRIPTS = Path(__file__).resolve().parent / "script"
CUTS = [
    "cut1_breath.tsv",
    "cut2_wait_go.tsv",
    "cut3_key_resume.tsv",
    "cut4_reverse.tsv",
]
PERMISSION_WINDOW = "com.android.permissioncontroller"  # 차량용 권한 창(Car UI 목록 — 버튼 id 없음, 글자 「허용」)
DEVICE_VIDEO = "/sdcard/rehearsal.mp4"
RECORD_LIMIT_S = 180  # screenrecord 상한
WARM_UP_S = (
    5.0  # 앱 실행 직후 메인 스레드 늦음(설계서 위험 절 — 1/4회 약 4초)을 컷 밖에서 흡수
)
BETWEEN_CUTS_S = 3.0


def wait_screen(text: str, limit_s: float = 20.0) -> None:
    deadline = time.monotonic() + limit_s
    while not screen_has(text):
        if time.monotonic() > deadline:
            raise RuntimeError(f"화면에 「{text}」 없음")
        time.sleep(0.5)


def fresh_install(apk: Path) -> None:
    """새 설치 — 지운 뒤 깔아 권한·자동 재개·오늘 카운트가 처음 상태. 실행 권한(-g)은 주지 않는다(권한 화면을 거침)."""
    subprocess.run(
        ["adb", "uninstall", APP], check=False, capture_output=True
    )  # 처음이면 없음
    adb("install", str(apk))


def grant_speed() -> None:
    """첫 실행 권한 안내 → [허용] → 시스템 창 「허용」(AAOS가 CAR_SPEED를 위치 묶음으로 묻는다)."""
    launch_app()
    wait_screen("허용")
    tap_text("허용")
    deadline = time.monotonic() + 10.0
    while PERMISSION_WINDOW not in (xml := screen_xml()):
        if time.monotonic() > deadline:
            raise RuntimeError("시스템 권한 창 없음")
        time.sleep(0.5)
    tap_text(
        "허용", xml
    )  # 덤프엔 맨 위 창(시스템 창)만 — 앱의 [허용]과 헷갈리지 않는다
    wait_screen("재생")


# 컷 2만 이어 듣기 위치를 남긴다 — 앱을 껐다 켜도 컷 1에서 멈춘 문장부터 읽는 장면(이어 듣기 실측)
KEEP_POSITION = {"cut2_wait_go.tsv"}


def relaunch(keep_position: bool = False) -> None:
    adb("shell", "am", "force-stop", "--user", "10", APP)
    if not keep_position:  # 매 컷 첫 문장부터(디버그 빌드만 run-as 가능)
        adb(
            "shell",
            "run-as",
            APP,
            "--user",
            "10",
            "rm",
            "-f",
            "shared_prefs/position.xml",
        )
    mark("relaunch")  # 앱 실행 → 첫 기어 콜백 지연을 잰다(화면의 첫 신호 대기 근거)
    launch_app()
    wait_screen("재생")
    time.sleep(WARM_UP_S)


def run_all(watch: LogWatch) -> None:
    reset_car()
    mark("rehearsal permission")
    grant_speed()
    for name in CUTS:
        reset_car()  # 지난 컷이 D로 끝나면 D 화면엔 「재생」이 없다 — P로 먼저
        relaunch(keep_position=name in KEEP_POSITION)
        run_cut(SCRIPTS / name, watch)
        time.sleep(BETWEEN_CUTS_S)
    mark("rehearsal end")


def record(video: Path, log_path: Path) -> None:
    adb("logcat", "-c")
    watch = LogWatch(log_path)
    recorder = subprocess.Popen(
        [
            "adb",
            "shell",
            "screenrecord",
            "--time-limit",
            str(RECORD_LIMIT_S),
            DEVICE_VIDEO,
        ]
    )
    try:
        run_all(watch)
    finally:
        adb("shell", "pkill", "-INT", "screenrecord")
        recorder.wait(timeout=20)
        watch.close()
    time.sleep(1.0)  # mp4 마무리 기록
    adb("pull", DEVICE_VIDEO, str(video))


def main(apk: Path, video: Path, log_path: Path) -> None:
    adb("forward", "tcp:8088", "tcp:8088")
    stop = threading.Event()
    threading.Thread(target=keep_vc_active, args=(stop,), daemon=True).start()
    try:
        time.sleep(2.0)
        reset_car()
        fresh_install(apk)
        record(video, log_path)
    finally:
        stop.set()


if __name__ == "__main__":
    main(Path(sys.argv[1]), Path(sys.argv[2]), Path(sys.argv[3]))
