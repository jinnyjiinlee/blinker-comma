"""자막(WebVTT·SRT)이 있는 음성 → 쉼표 팩. 큐 하나 = 문장 하나. 큐 경계로 자르기만 하고 내용은 바꾸지 않는다.

팟캐스트 대본(Podcasting 2.0 transcript), 강의 자막처럼 이미 문장 시각이 있는 콘텐츠에 쓴다
— 받아쓰기 없이 바로 엔진의 문장 경계가 된다(docs/INTEGRATION.md 2절).

사용: python3 tools/pack_from_transcript.py <음성> <자막.vtt|.srt> <out_dir> <제목(6자 이내)> <출처>
"""

import json
import re
import sys
from pathlib import Path

from cut_pack import cut_clip

CUE_TIME = re.compile(
    r"(\d+:)?(\d{2}):(\d{2})[.,](\d{3})\s*-->\s*(\d+:)?(\d{2}):(\d{2})[.,](\d{3})"
)


def seconds(hours: str | None, minutes: str, secs: str, millis: str) -> float:
    return (
        int((hours or "0:")[:-1]) * 3600
        + int(minutes) * 60
        + int(secs)
        + int(millis) / 1000
    )


def parse_block(block: str) -> tuple[float, float, str] | None:
    """큐 한 덩어리 → (시작, 끝, 글자). 시각 줄이 없거나 글자가 비면 None."""
    lines = block.strip().splitlines()
    for i, line in enumerate(lines):
        m = CUE_TIME.search(line)
        if m:
            start, end = seconds(*m.groups()[:4]), seconds(*m.groups()[4:])
            text = " ".join(t.strip() for t in lines[i + 1 :] if t.strip())
            return (start, end, text) if text and end > start else None
    return None


def read_cues(path: Path) -> list[tuple[float, float, str]]:
    """번호 줄·WEBVTT 머리·빈 줄은 건너뛴다."""
    blocks = re.split(r"\n\s*\n", path.read_text(encoding="utf-8").replace("\r", ""))
    return [cue for block in blocks if (cue := parse_block(block))]


def to_clip(
    audio: Path, out_dir: Path, order: int, cue: tuple[float, float, str]
) -> dict:
    start, end, text = cue
    name = f"s{order:02d}.m4a"
    return {
        "file": name,
        "text": text,
        "durationMs": cut_clip(audio, start, end, out_dir / name),
    }


def main(audio: Path, transcript: Path, out_dir: Path, title: str, credit: str) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    clips = [
        to_clip(audio, out_dir, order, cue)
        for order, cue in enumerate(read_cues(transcript), start=1)
    ]
    pack = {"title": title, "credit": credit, "clips": clips}
    (out_dir / "pack.json").write_text(
        json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"{len(clips)}개 문장 → {out_dir}")


if __name__ == "__main__":
    main(
        Path(sys.argv[1]),
        Path(sys.argv[2]),
        Path(sys.argv[3]),
        sys.argv[4],
        sys.argv[5],
    )
