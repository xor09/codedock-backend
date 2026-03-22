#!/usr/bin/env bash
set -euo pipefail

LANG=$1
SRC=$2
STDIN=$3
TIMEOUT=$4

ABS_SRC="$(realpath "$SRC")"
ABS_STDIN="$(realpath "$STDIN")"

WORKDIR_HOST="$(dirname "$ABS_SRC")"
FILENAME="$(basename "$ABS_SRC")"
STDIN_FILE="$(basename "$ABS_STDIN")"

case "$LANG" in
  cpp)
    IMAGE="runner-cpp"
    CMD="g++ \"$FILENAME\" -O2 -std=c++17 -o /tmp/a.out && timeout ${TIMEOUT}s /tmp/a.out < \"$STDIN_FILE\""
    ;;
  java)
     IMAGE="runner-java"
        MAIN="${FILENAME%.*}"
        CMD="javac -d /tmp \"$FILENAME\" \
         && timeout ${TIMEOUT}s java -cp /tmp \"$MAIN\" < /home/runner/work/stdin.txt"
        ;;
  python)
    IMAGE="runner-python"
    CMD="timeout ${TIMEOUT}s python \"$FILENAME\" < \"$STDIN_FILE\""
    ;;
  *)
    echo \"Unsupported language\"
    exit 1
    ;;
esac

docker run --rm \
  -v "$WORKDIR_HOST":/home/runner/work:ro \
  -v /tmp:/tmp:rw \
  -w /home/runner/work \
  --network none \
  --cpus "0.5" \
  --memory "256m" \
  "$IMAGE" bash -lc "$CMD"
