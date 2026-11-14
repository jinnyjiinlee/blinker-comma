"""문장을 쉼표 자리에서 쓸 만한 길이의 조각으로 — build_long_pack.py가 쓴다.

3초가 넘는 문장은 원문 쉼표(,;:)에서, 그래도 6초가 넘으면 받아쓰기 구간(segment) 경계 — 대개 숨 쉰 자리 — 에서 나눈다.
앞뒤 조각이 1초보다 짧아지면 나누지 않는다.
"""

import json
import re
from collections.abc import Callable
from pathlib import Path

PHRASE_END = re.compile(r'[,;:]"?$')
LONG_S = 3.0
SEGMENT_SPLIT_S = 6.0
MIN_PART_S = 1.0


def load_words(json_path: Path) -> list[dict]:
    """단어 목록. 받아쓰기 구간의 마지막 단어엔 seg_end 표시."""
    words: list[dict] = []
    for seg in json.loads(json_path.read_text(encoding="utf-8"))["segments"]:
        words += [dict(w, seg_end=False) for w in seg["words"]]
        if words:
            words[-1]["seg_end"] = True
    return words


def span(words: list[dict]) -> float:
    return words[-1]["end"] - words[0]["start"]


def split_where(
    words: list[dict], longer_than: float, is_cut: Callable[[dict], bool]
) -> list[list[dict]]:
    """longer_than보다 긴 조각을 is_cut 단어 뒤에서 나눈다(앞뒤 MIN_PART_S 이상일 때만)."""
    if span(words) <= longer_than:
        return [words]
    parts: list[list[dict]] = []
    current: list[dict] = []
    for i, word in enumerate(words):
        current.append(word)
        rest = words[i + 1 :]
        if is_cut(word) and rest and min(span(current), span(rest)) >= MIN_PART_S:
            parts.append(current)
            current = []
    return [*parts, current]


def split_phrases(sentence: list[dict]) -> list[list[dict]]:
    by_comma = split_where(
        sentence, LONG_S, lambda w: bool(PHRASE_END.search(w["word"].strip()))
    )
    return [
        p
        for part in by_comma
        for p in split_where(part, SEGMENT_SPLIT_S, lambda w: w["seg_end"])
    ]
