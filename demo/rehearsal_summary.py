"""리허설 로그 → 컷별 「첫 anchor부터 몇 초에 무슨 줄」 표. 두 로그를 주면 같은 결과인지 비교한다.

사용: python3 demo/rehearsal_summary.py <로그 1> [<로그 2>]
같음 = 컷마다 줄 순서·글자가 같고 시각 차이가 TOLERANCE_S 이하. 비교 대상은 엔진 로그(Comma)·⏸(CommaGlyph)·
실제 소리 시작(CommaPlayer start s=N) — 신호 원본(CommaSignal)의 at=·열기 ms처럼 매번 다른 값은 뺀다.
"""

import re
import sys
from pathlib import Path

TOLERANCE_S = 0.15
TAIL_S = 1.5  # 컷 끝 표지(마지막 대본 줄) 뒤 — ⓓ(OFF + 0.8초)까지 담고 다음 컷 준비(P 복귀·재실행)는 뺀다
KEPT = re.compile(r"^(Comma: .*|CommaGlyph: .*|CommaPlayer: start s=\d+)")
CUT_MARK = re.compile(r"^CommaStage: cut (\S+) prep")
END_MARK = re.compile(r"^CommaStage: cut \S+ end")
ANCHOR_MARK = re.compile(r"^CommaStage: anchor \S+ \| (.*)$")
LINE = re.compile(r"^(\d\d):(\d\d):(\d\d\.\d+) (.*)$")
Event = tuple[float, str]


def read_log(path: Path) -> list[Event]:
    events = []
    for raw in path.read_text(encoding="utf-8").splitlines():
        if found := LINE.match(raw):
            hours, minutes, seconds, text = found.groups()
            events.append(
                (int(hours) * 3600 + int(minutes) * 60 + float(seconds), text)
            )
    return events


def split_cuts(events: list[Event]) -> dict[str, list[Event]]:
    cuts: dict[str, list[Event]] = {}
    current = None
    for at, text in events:
        if found := CUT_MARK.match(text):
            current = found[1]
            cuts[current] = []
        elif current is not None:
            cuts[current].append((at, text))
    return cuts


def zero_of(events: list[Event]) -> float:
    """첫 anchor 줄의 시각 — conductor가 표지로 남긴 글자와 같은 줄(표지보다 앞)."""
    mark_at, anchor_text = next(
        (at, found[1]) for at, text in events if (found := ANCHOR_MARK.match(text))
    )
    return max(at for at, text in events if text == anchor_text and at <= mark_at)


def timeline(events: list[Event]) -> list[Event]:
    zero = zero_of(events)
    until = next(at for at, text in events if END_MARK.match(text)) + TAIL_S
    kept = [(at - zero, KEPT.match(text)) for at, text in events if at <= until]
    return [(round(at, 3), found[1]) for at, found in kept if found and at >= -0.001]


def compare(first: list[Event], second: list[Event]) -> list[str]:
    if [text for _, text in first] != [text for _, text in second]:
        return ["줄 순서·글자가 다름"]
    return [
        f"{text}: {a:.2f} vs {b:.2f}"
        for (a, text), (b, _) in zip(first, second)
        if abs(a - b) > TOLERANCE_S
    ]


def main(paths: list[Path]) -> int:
    runs = [
        {name: timeline(cut) for name, cut in split_cuts(read_log(path)).items()}
        for path in paths
    ]
    for name, lines in runs[0].items():
        print(f"== {name}")
        others = [run.get(name, []) for run in runs[1:]]
        for index, (at, text) in enumerate(lines):
            print(
                f"  {at:6.2f}"
                + "".join(f" {o[index][0]:6.2f}" for o in others if index < len(o))
                + f"  {text}"
            )
    problems = [
        f"{name}: {p}"
        for run in runs[1:]
        for name in runs[0]
        for p in compare(runs[0][name], run.get(name, []))
    ]
    print(
        "\n".join(problems) or (f"같음(차이 ≤ {TOLERANCE_S}s)" if len(runs) > 1 else "")
    )
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main([Path(arg) for arg in sys.argv[1:]]))
