#!/bin/bash
# 측정용 합성음 팩 — 실제 낭독 팩(퍼블릭 도메인 녹음 자르기)이 들어오기 전 D2 신호·완료 콜백 실측용.
# 문장 대신 길이가 정해진 사인파 클립. 낭독 팩이 생기면 같은 폴더를 통째로 덮어쓴다.
set -e
OUT="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/assets/packs/en1"
mkdir -p "$OUT"
DURATIONS=(2.6 3.0 4.0 2.2 5.0 3.5 2.8 4.5)
CLIPS=""
for i in "${!DURATIONS[@]}"; do
    n=$(printf "s%02d" $((i + 1)))
    d=${DURATIONS[$i]}
    ffmpeg -loglevel error -y -f lavfi -i "sine=frequency=440:duration=$d" -ac 1 -ar 22050 -c:a aac -b:a 64k "$OUT/$n.m4a"
    ms=$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$OUT/$n.m4a" | awk '{printf "%d", $1 * 1000}')
    CLIPS+="    {\"file\": \"$n.m4a\", \"text\": \"probe tone $((i + 1))\", \"durationMs\": $ms},"$'\n'
done
printf '{\n  "title": "측정 팩",\n  "credit": "측정용 합성음(낭독 팩 교체 전)",\n  "clips": [\n%s  ]\n}\n' "${CLIPS%,$'\n'}"$'\n' > "$OUT/pack.json"
cat "$OUT/pack.json"
