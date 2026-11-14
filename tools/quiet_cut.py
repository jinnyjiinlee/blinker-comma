"""자르는 자리를 실제로 가장 조용한 순간으로 옮긴다 — 받아쓰기 단어 시각은 수십 ms씩 어긋나 말 중간을 자를 수 있다.

원본을 8kHz 모노로 풀어 10ms마다 음량(RMS)을 재고, 경계 앞뒤 창 안에서 음량이 가장 낮은 10ms를 고른다.
"""

import array
import subprocess
from pathlib import Path

RATE = 8000
FRAME = RATE // 100  # 10ms
WINDOW_S = 0.2  # 경계 앞뒤로 이만큼 넓혀 찾는다


DECODE = [
    "ffmpeg",
    "-v",
    "error",
    "-f",
    "s16le",
    "-ac",
    "1",
    "-ar",
    "8000",
    "-",
]  # 입력은 -v error 뒤에 끼운다


def envelope(audio: Path) -> list[float]:
    """10ms 프레임별 RMS."""
    cmd = [*DECODE[:3], "-i", str(audio), *DECODE[3:]]
    pcm = array.array("h", subprocess.run(cmd, capture_output=True, check=True).stdout)
    frames = range(0, len(pcm) - FRAME, FRAME)
    return [(sum(x * x for x in pcm[i : i + FRAME]) / FRAME) ** 0.5 for i in frames]


def quietest(env: list[float], start_s: float, end_s: float) -> float:
    """[start_s - WINDOW_S, end_s + WINDOW_S]에서 가장 조용한 10ms의 가운데 시각(초)."""
    lo = max(0, int((min(start_s, end_s) - WINDOW_S) * 100))
    hi = min(len(env) - 1, int((max(start_s, end_s) + WINDOW_S) * 100))
    best = min(range(lo, hi + 1), key=env.__getitem__, default=lo)
    return best / 100 + 0.005
