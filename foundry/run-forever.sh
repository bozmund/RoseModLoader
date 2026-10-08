#!/usr/bin/env sh
# Runs the foundry around the clock: a batch of tasks, a short pause, repeat. Stop with Ctrl+C.
# Usage: foundry/run-forever.sh [extra rose foundry run options, e.g. --model mony/qwen3.8-27b-q5]
cd "$(dirname "$0")/.." || exit 1
while true; do
  ./rose foundry run --max 20 --attempts 2 "$@"
  echo "[foundry] batch finished $(date); next batch in 5 minutes"
  sleep 300
done
