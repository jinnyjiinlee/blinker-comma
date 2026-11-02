"""무대 대본(demo/script의 TSV) 읽기 — conductor·리허설 요약이 같이 쓴다. 형식은 docs/ARCHITECTURE.md 6절."""

from dataclasses import dataclass
from pathlib import Path

ANCHOR = "-"
END_PREFIX = "end-"
# anchor 글자 → 로그에서 찾을 줄. 「문장 시작」은 엔진 명령(clipStart)이 아니라 실제 소리 시작(설계서 위험 절)
ANCHOR_PATTERNS = {
    "clipStart": r"^CommaPlayer: start s=(\d+) fromMs=(\d+) clipMs=(\d+)",
    "→ hold": r"^Comma: .*→ hold",
}


@dataclass(frozen=True)
class Row:
    at: str
    hand: str
    signal: str
    value: str
    memo: str

    @property
    def is_anchor(self) -> bool:
        return self.at == ANCHOR


def read_rows(path: Path) -> list[Row]:
    lines = path.read_text(encoding="utf-8").splitlines()
    cells = [
        line.split("\t") for line in lines if line.strip() and not line.startswith("#")
    ]
    return [Row(*(cell + [""] * 5)[:5]) for cell in cells]


def split_prep(rows: list[Row]) -> tuple[list[Row], list[Row]]:
    """첫 anchor 전 줄 = 녹화 전 준비(at 0, 차례대로), 나머지 = 본 무대."""
    first = next(index for index, row in enumerate(rows) if row.is_anchor)
    return rows[:first], rows[first:]


def delay_after_anchor(at: str, remaining_clip_s: float | None) -> float:
    """숫자 = anchor부터 초 · end-X = anchor 문장이 끝나기 X초 전(남은 길이는 anchor 줄의 clipMs − fromMs)."""
    if not at.startswith(END_PREFIX):
        return float(at)
    if remaining_clip_s is None:
        raise ValueError(f"{at}: 바로 위 anchor가 문장 시작(clipStart)이 아님")
    return remaining_clip_s - float(at.removeprefix(END_PREFIX))
