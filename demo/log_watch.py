"""앱 로그(logcat) 받아 두기 — anchor가 「이 줄이 새로 찍힐 때까지」 기다리는 곳. 파일에도 그대로 남긴다(실측 근거)."""

import re
import subprocess
import threading
import time
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

TAGS = [
    "Comma",
    "CommaSignal",
    "CommaPlayer",
    "CommaDeck",
    "CommaFocus",
    "CommaMedia",
    "CommaInject",
    "CommaGlyph",
]
STAGE_TAG = "CommaStage"
LINE = re.compile(r"^\s*(\d+\.\d+)\s+\d+\s+\d+\s+[VDIWEF]\s+(\S+)\s*: (.*)$")


@dataclass(frozen=True)
class LogLine:
    device_s: float  # 에뮬레이터 시계(epoch 초)
    text: str  # 「태그: 내용」


class LogWatch:
    """logcat을 줄 단위로 받는다. 시각은 에뮬레이터 시계 — 받는 데 걸린 지연이 anchor에 섞이지 않게."""

    def __init__(self, out: Path) -> None:
        filters = [f"{tag}:V" for tag in [*TAGS, STAGE_TAG]] + [
            "AndroidRuntime:E",
            "*:S",
        ]
        self.lines: list[LogLine] = []
        # 호스트 시계 − 에뮬레이터 시계(받은 줄 중 가장 작은 값)
        self.offset_s = float("inf")
        self._sink = out.open("w", encoding="utf-8")
        self._process = subprocess.Popen(
            ["adb", "logcat", "-v", "epoch", *filters],
            stdout=subprocess.PIPE,
            text=True,
            encoding="utf-8",
        )
        threading.Thread(target=self._pump, daemon=True).start()

    def _pump(self) -> None:
        for raw in self._process.stdout or []:
            found = LINE.match(raw)
            if not found:
                continue
            line = LogLine(float(found[1]), f"{found[2]}: {found[3]}")
            self.offset_s = min(self.offset_s, time.time() - line.device_s)
            clock = datetime.fromtimestamp(line.device_s).strftime("%H:%M:%S.%f")[:-3]
            self._sink.write(f"{clock} {line.text}\n")
            self._sink.flush()
            self.lines.append(line)

    def wait_for(
        self, pattern: str, since: int, limit_s: float = 30.0
    ) -> tuple[LogLine, re.Match[str]]:
        """since번째 줄부터 pattern이 처음 맞는 줄. 못 찾으면 멈춘다(가짜로 지나가지 않게)."""
        deadline = time.monotonic() + limit_s
        while time.monotonic() < deadline:
            for line in self.lines[since:]:
                if found := re.search(pattern, line.text):
                    return line, found
            time.sleep(0.01)
        raise RuntimeError(f"로그에 「{pattern}」 없음({limit_s:.0f}초)")

    def host_time(self, line: LogLine) -> float:
        return line.device_s + self.offset_s

    def close(self) -> None:
        self._process.terminate()
        self._sink.close()
