"""여러 장(트랙)을 이어 긴 낭독 팩 하나로 — 출퇴근 길이(약 1시간) 콘텐츠.

장마다 cut_pack.py와 같은 방식(word-level STT 타임스탬프의 문장 끝 부호)으로 자르고, 번호를 이어 붙인다.
3초가 넘는 문장은 원문의 쉼표(,;:) 자리에서 구절로 나눈다 — 깜빡이 쉼표가 말의 쉼표에서 숨을 참게.
LibriVox 장 머리말(「Section N of … This is a LibriVox recording … by …」)과 꼬리말(「End of section N」)은
문장 내용으로 찾아 뺀다 — 낭독자 이름이 팩에 들어가지 않게.

사용: python3 tools/build_long_pack.py <out_dir> <title> <credit> <mp3> <stt.json> [<mp3> <stt.json> ...]
"""

import json
import re
import sys
from pathlib import Path

from cut_pack import CutContext, load_words, split_sentences, to_clip

PHRASE_END = re.compile(r'[,;:]"?$')
LONG_S = 3.0  # 이보다 긴 문장은 원문의 쉼표 자리에서 구절로 나눈다
MIN_PART_S = 1.0  # 너무 짧은 조각은 만들지 않는다
INTRO_END = re.compile(r"for children,? by", re.IGNORECASE)
OUTRO = re.compile(r"end of (the )?section", re.IGNORECASE)


def span(words: list[dict]) -> float:
    return words[-1]["end"] - words[0]["start"]


def can_cut(word: dict, current: list[dict], rest: list[dict]) -> bool:
    """원문 쉼표 뒤이고, 앞뒤 조각이 둘 다 MIN_PART_S 이상일 때만 자른다."""
    is_phrase_end = bool(PHRASE_END.search(word["word"].strip()))
    return (
        is_phrase_end
        and bool(rest)
        and span(current) >= MIN_PART_S
        and span(rest) >= MIN_PART_S
    )


def split_phrases(sentence: list[dict]) -> list[list[dict]]:
    """긴 문장을 원문 쉼표(,;:) 뒤에서 나눈다."""
    if span(sentence) <= LONG_S:
        return [sentence]
    parts: list[list[dict]] = []
    current: list[dict] = []
    for i, word in enumerate(sentence):
        current.append(word)
        if can_cut(word, current, sentence[i + 1 :]):
            parts.append(current)
            current = []
    return [*parts, current]


def keep_range(sentences: list[list[dict]]) -> range:
    texts = ["".join(w["word"] for w in s) for s in sentences]
    first = next((i + 1 for i, t in enumerate(texts[:12]) if INTRO_END.search(t)), 0)
    last = next(
        (i for i, t in enumerate(texts) if i >= first and OUTRO.search(t)), len(texts)
    )
    return range(first, last)


def cut_track(
    audio: Path, words_json: Path, out_dir: Path, first_order: int
) -> list[dict]:
    """한 장 — 머리말·꼬리말을 빼고 문장(긴 문장은 구절)마다 클립 하나."""
    sentences = split_sentences(load_words(words_json))
    keep = keep_range(sentences)
    parts = [part for index in keep for part in split_phrases(sentences[index])]
    ctx = CutContext(parts, sentences[-1][-1]["end"] + 1.0, {}, audio, out_dir)
    clips = [to_clip(ctx, index, first_order + index) for index in range(len(parts))]
    dropped = f"머리말 {keep.start}·꼬리말 {len(sentences) - keep.stop}문장 뺌"
    print(f"{audio.name}: {len(keep)}문장 → {len(parts)}조각 ({dropped})")
    return clips


def main(
    out_dir: Path, title: str, credit: str, pairs: list[tuple[Path, Path]]
) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    clips: list[dict] = []
    for audio, words_json in pairs:
        clips += cut_track(audio, words_json, out_dir, len(clips) + 1)
    pack = {"title": title, "credit": credit, "clips": clips}
    (out_dir / "pack.json").write_text(
        json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    total = sum(c["durationMs"] for c in clips) / 1000
    print(f"합계 {len(clips)}문장 · {total:.1f}초 → {out_dir}")


if __name__ == "__main__":
    rest = sys.argv[4:]
    main(
        Path(sys.argv[1]),
        sys.argv[2],
        sys.argv[3],
        [(Path(a), Path(b)) for a, b in zip(rest[::2], rest[1::2])],
    )
