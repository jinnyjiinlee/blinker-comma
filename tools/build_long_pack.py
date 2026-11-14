"""여러 장(트랙)을 이어 긴 낭독 팩 하나로 — 출퇴근 길이(약 1시간) 콘텐츠.

장마다 word-level 받아쓰기의 문장 끝 부호로 자르고(긴 문장은 pack_split.py 규칙으로 구절), 번호를 이어 붙인다.
자르는 자리는 경계 근처에서 가장 조용한 순간(quiet_cut.py). LibriVox 장 머리말(녹음 안내·낭독자 소개)과
꼬리말은 문장 내용으로 찾아 뺀다 — 낭독자 이름이 팩에 들어가지 않게. 기본 규칙은 이솝 영어 녹음이고,
다른 녹음은 환경변수 PACK_INTRO·PACK_OUTRO(정규식)로 바꾼다.

사용: python3 tools/build_long_pack.py <out_dir> <title> <credit> <mp3> <stt.json> [<mp3> <stt.json> ...]
"""

import json
import os
import re
import sys
from itertools import pairwise
from pathlib import Path

from cut_pack import cut_clip, split_sentences
from pack_split import load_words, split_phrases
from quiet_cut import envelope, quietest

INTRO_END = re.compile(
    os.environ.get("PACK_INTRO", r"for children,? by"), re.IGNORECASE
)
OUTRO = re.compile(
    os.environ.get("PACK_OUTRO", r"end of (the )?section"), re.IGNORECASE
)


def keep_range(sentences: list[list[dict]]) -> range:
    """머리말 = 앞 12문장 중 마지막으로 INTRO_END가 맞는 문장까지, 꼬리말 = 끝 3문장 중 OUTRO가 처음 맞는 문장부터."""
    texts = ["".join(w["word"] for w in s) for s in sentences]
    heads = [i + 1 for i, t in enumerate(texts[:12]) if INTRO_END.search(t)]
    first = heads[-1] if heads else 0
    tail = range(max(first, len(texts) - 3), len(texts))
    return range(first, next((i for i in tail if OUTRO.search(texts[i])), len(texts)))


def cut_points(parts: list[list[dict]], env: list[float]) -> list[float]:
    """조각 사이 경계마다 가장 조용한 순간. 처음은 첫 단어 0.05초 앞, 끝은 끝 단어 0.15초 뒤."""
    inner = [quietest(env, a[-1]["end"], b[0]["start"]) for a, b in pairwise(parts)]
    return [max(0.0, parts[0][0]["start"] - 0.05), *inner, parts[-1][-1]["end"] + 0.15]


def clip_of(
    audio: Path, out_dir: Path, order: int, part: list[dict], at: tuple[float, float]
) -> dict:
    name = f"s{order:02d}.m4a"
    text = "".join(w["word"] for w in part).strip()
    return {
        "file": name,
        "text": text,
        "durationMs": cut_clip(audio, at[0], at[1], out_dir / name),
    }


def cut_track(audio: Path, words_json: Path, out_dir: Path, first: int) -> list[dict]:
    """한 장 — 머리말·꼬리말을 빼고 문장(긴 문장은 구절)마다 클립 하나."""
    sentences = split_sentences(load_words(words_json))
    keep = keep_range(sentences)
    parts = [part for index in keep for part in split_phrases(sentences[index])]
    at = cut_points(parts, envelope(audio))
    clips = [
        clip_of(audio, out_dir, first + i, p, at[i : i + 2])
        for i, p in enumerate(parts)
    ]
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
    print(
        f"합계 {len(clips)}조각 · {sum(c['durationMs'] for c in clips) / 1000:.1f}초 → {out_dir}"
    )


if __name__ == "__main__":
    rest = sys.argv[4:]
    main(
        Path(sys.argv[1]),
        sys.argv[2],
        sys.argv[3],
        [(Path(a), Path(b)) for a, b in zip(rest[::2], rest[1::2])],
    )
