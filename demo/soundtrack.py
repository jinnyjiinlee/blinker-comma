"""리허설 녹화에 낭독 소리를 입힌다 — 에뮬레이터는 -no-audio라 녹화에 소리가 없다.

앱 로그의 「CommaPlayer: start s=N fromMs=F」 시각에 팩의 그 문장 파일을 F부터 놓고,
다음 재생 줄·끝 줄·relaunch(앱 강제 종료)·리허설 끝 중 가장 이른 시각에서 자른다.
즉 영상의 소리는 앱이 실제로 재생 명령을 낸 시각·구간을 그대로 옮긴 것이다(합성 표기는 영상 타이틀에).

사용: python3 demo/soundtrack.py <녹화 mp4> <로그> <팩 폴더> <영상 0초의 로그 시각 HH:MM:SS.mmm> <출력 mp4>
  영상 0초의 로그 시각은 기어 D 전환 프레임처럼 화면이 바뀌는 순간과 로그 줄을 맞춰 정한다.
"""

import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

START = re.compile(r"^(\S+) CommaPlayer: start s=(\d+) fromMs=(\d+)")
CUT = re.compile(
    r"^(\S+) (CommaPlayer: (end|pause|stop)|CommaStage: (relaunch|rehearsal end))"
)
SAMPLE_RATE = 48000


@dataclass(frozen=True)
class Piece:
    sentence: int  # 1부터(로그 s=N)
    at_s: float  # 영상 시각
    from_s: float  # 문장 파일 안 시작점
    length_s: float


def seconds(clock: str) -> float:
    hours, minutes, rest = clock.split(":")
    return int(hours) * 3600 + int(minutes) * 60 + float(rest)


def pieces(log: Path, zero: float) -> list[Piece]:
    lines = log.read_text(encoding="utf-8").splitlines()
    found: list[Piece] = []
    for number, line in enumerate(lines):
        start = START.match(line)
        if not start:
            continue
        begin = seconds(start[1])
        until = next(
            (
                seconds(m[1])
                for later in lines[number + 1 :]
                if (m := CUT.match(later) or START.match(later))
            ),
            begin + 30.0,
        )
        found.append(
            Piece(int(start[2]), begin - zero, int(start[3]) / 1000, until - begin)
        )
    return found


def mix(video: Path, pack: Path, found: list[Piece], out: Path) -> None:
    inputs: list[str] = []
    filters: list[str] = []
    for i, piece in enumerate(found):
        inputs += ["-i", str(pack / f"s{piece.sentence:02d}.m4a")]
        delay = max(0, round(piece.at_s * 1000))
        filters.append(
            f"[{i + 1}:a]atrim=start={piece.from_s:.3f}:duration={piece.length_s:.3f},"
            f"asetpts=PTS-STARTPTS,aresample={SAMPLE_RATE},adelay={delay}:all=1[a{i}]"
        )
    labels = "".join(f"[a{i}]" for i in range(len(found)))
    filters.append(f"{labels}amix=inputs={len(found)}:normalize=0[aout]")
    subprocess.run(
        [
            "ffmpeg",
            "-v",
            "error",
            "-y",
            "-i",
            str(video),
            *inputs,
            "-filter_complex",
            ";".join(filters),
            "-map",
            "0:v:0",
            "-map",
            "[aout]",
            "-c:v",
            "copy",
            "-c:a",
            "aac",
            "-b:a",
            "160k",
            "-shortest",
            str(out),
        ],
        check=True,
    )


def main(video: Path, log: Path, pack: Path, zero_clock: str, out: Path) -> None:
    found = pieces(log, seconds(zero_clock))
    for piece in found:
        print(
            f"{piece.at_s:7.2f}s  s={piece.sentence}  from {piece.from_s:.2f}s  {piece.length_s:.2f}s"
        )
    mix(video, pack, found, out)


if __name__ == "__main__":
    main(
        Path(sys.argv[1]),
        Path(sys.argv[2]),
        Path(sys.argv[3]),
        sys.argv[4],
        Path(sys.argv[5]),
    )
