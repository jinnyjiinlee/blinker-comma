"""받아쓰기(word-level JSON) → WebVTT. 큐 하나 = 문장 하나(머리말·꼬리말 뺌) — 샘플 앱(:sample)의 자막 경계.

사용: python3 tools/vtt_from_stt.py <stt.json> <out.vtt>
"""

import sys
from pathlib import Path

from build_long_pack import keep_range
from cut_pack import load_words, split_sentences


def stamp(seconds: float) -> str:
    ms = round(seconds * 1000)
    return f"{ms // 3600000:02d}:{ms // 60000 % 60:02d}:{ms // 1000 % 60:02d}.{ms % 1000:03d}"


def main(words_json: Path, out: Path) -> None:
    sentences = split_sentences(load_words(words_json))
    blocks = ["WEBVTT", ""]
    for index in keep_range(sentences):
        words = sentences[index]
        text = "".join(w["word"] for w in words).strip()
        blocks += [
            f"{stamp(words[0]['start'])} --> {stamp(words[-1]['end'] + 0.1)}",
            text,
            "",
        ]
    out.write_text("\n".join(blocks), encoding="utf-8")
    print(f"{(len(blocks) - 2) // 3}개 큐 → {out}")


if __name__ == "__main__":
    main(Path(sys.argv[1]), Path(sys.argv[2]))
