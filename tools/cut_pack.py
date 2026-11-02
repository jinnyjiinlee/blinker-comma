"""콘텐츠 팩 자르기 — 공공 도메인 낭독 원본(mp3, tools/raw/)을 문장 단위로 잘라 APK 에셋 팩을 만든다
(SPEC 「콘텐츠 팩」). 무음 구간 검출 대신 word-level STT 타임스탬프로 문장 끝(.!?)을 찾는다 — 자막(text)이
실제 발화와 안 어긋난다. 준비: whisper 원본.mp3 --word_timestamps True --output_format json
사용: python3 tools/cut_pack.py <원본.mp3> <whisper.json> <out_dir> <title> <credit>
  [--skip-start N] [--skip-end N] [--fix "옛말=새말" ...]  (오인식 교정 — 뜻은 안 바꿈)
"""

import argparse
import json
import re
import subprocess
from dataclasses import dataclass
from pathlib import Path

Sentences = list[list[dict]]
SENTENCE_END = re.compile(r'[.!?]"?$')
PRE_PAD_S = 0.05
# 끝 무음 패딩 — ClipPlayer가 durationMs를 단조 시계로 재므로, 패딩이 없으면 끝소리가 잘릴 수 있다(D2 실측).
POST_PAD_S = 0.15
FFMPEG_BASE = ["ffmpeg", "-loglevel", "error", "-y"]
AUDIO_FLAGS = ["-ac", "1", "-ar", "22050", "-c:a", "aac", "-b:a", "64k"]
PROBE = "ffprobe -v error -show_entries format=duration -of csv=p=0".split()  # noqa: SIM905


@dataclass(frozen=True)
class CutContext:
    sentences: list[list[dict]]
    track_end_s: float
    fixes: dict[str, str]
    audio: Path
    out_dir: Path


def load_words(json_path: Path) -> list[dict]:
    data = json.loads(json_path.read_text(encoding="utf-8"))
    return [word for seg in data["segments"] for word in seg["words"]]


def split_sentences(words: list[dict]) -> list[list[dict]]:
    sentences: list[list[dict]] = []
    current: list[dict] = []
    for word in words:
        current.append(word)
        if SENTENCE_END.search(word["word"].strip()):
            sentences.append(current)
            current = []
    if current:
        sentences.append(current)
    return sentences


def to_text(sentence: list[dict], fixes: dict[str, str]) -> str:
    text = "".join(word["word"] for word in sentence).strip()
    for wrong, right in fixes.items():
        text = text.replace(wrong, right)
    return text


def clip_bounds(sentences: Sentences, index: int, end_s: float) -> tuple[float, float]:
    """패딩은 앞뒤 문장 쪽으로 안 넘친다(말소리를 안 먹게)."""
    start = sentences[index][0]["start"] - PRE_PAD_S
    end = sentences[index][-1]["end"] + POST_PAD_S
    prev_end = sentences[index - 1][-1]["end"] if index > 0 else 0.0
    rest = sentences[index + 1 :]
    next_start = rest[0][0]["start"] if rest else end_s
    return max(start, prev_end), min(end, next_start)


def cut_clip(src: Path, start: float, end: float, out: Path) -> int:
    """잘라 재인코딩(mono 22050Hz AAC 64k, 기존 팩과 같은 포맷)하고 실제 길이(ms)를 돌려준다."""
    window = ["-ss", f"{start:.3f}", "-to", f"{end:.3f}"]
    cmd = [*FFMPEG_BASE, "-i", str(src), *window, *AUDIO_FLAGS, str(out)]
    subprocess.run(cmd, check=True)
    probed = subprocess.run([*PROBE, str(out)], capture_output=True, check=True)
    return round(float(probed.stdout.decode().strip()) * 1000)


def to_clip(ctx: CutContext, index: int, order: int) -> dict:
    start, end = clip_bounds(ctx.sentences, index, ctx.track_end_s)
    name = f"s{order:02d}.m4a"
    duration_ms = cut_clip(ctx.audio, start, end, ctx.out_dir / name)
    text = to_text(ctx.sentences[index], ctx.fixes)
    return {"file": name, "text": text, "durationMs": duration_ms}


def build_pack(args: argparse.Namespace) -> dict:
    fixes = dict(item.split("=", 1) for item in args.fix)
    sentences = split_sentences(load_words(args.words_json))
    track_end_s = sentences[-1][-1]["end"] + 1.0
    keep = range(args.skip_start, len(sentences) - args.skip_end)
    args.out_dir.mkdir(parents=True, exist_ok=True)
    ctx = CutContext(sentences, track_end_s, fixes, args.audio, args.out_dir)
    clips = [to_clip(ctx, index, order) for order, index in enumerate(keep, start=1)]
    return {"title": args.title, "credit": args.credit, "clips": clips}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("audio", type=Path)
    parser.add_argument("words_json", type=Path)
    parser.add_argument("out_dir", type=Path)
    parser.add_argument("title")
    parser.add_argument("credit")
    parser.add_argument("--skip-start", type=int, default=0)
    parser.add_argument("--skip-end", type=int, default=0)
    parser.add_argument("--fix", action="append", default=[])
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    pack = build_pack(args)
    text = json.dumps(pack, ensure_ascii=False, indent=2) + "\n"
    (args.out_dir / "pack.json").write_text(text, encoding="utf-8")
    print(f"{len(pack['clips'])}개 문장 → {args.out_dir}")


if __name__ == "__main__":
    main()
