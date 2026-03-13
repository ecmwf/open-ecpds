#!/usr/bin/env bash
set -e

# Load optional environment overrides
ENV_FILE="/etc/ecpds/ai.cnf"
if [ -f "$ENV_FILE" ]; then
  echo "[entrypoint] Loading environment overrides from $ENV_FILE"
  set -a
  source "$ENV_FILE"
  set +a
fi

# Log configuration
echo "[entrypoint] Ollama configuration:"
env | grep OLLAMA_ | sort

# Start Ollama server in background
NUMA_NODES=$(lscpu | grep "NUMA node(s)" | awk '{print $3}')
if command -v numactl >/dev/null 2>&1 && [ "$NUMA_NODES" -gt 1 ]; then
    echo "[entrypoint] Starting Ollama with NUMA interleave..."
    ollama_cmd="numactl --interleave=all ollama serve"
else
    echo "[entrypoint] NUMA not available, starting Ollama normally..."
    ollama_cmd="ollama serve"
fi

# Run Ollama in background and capture PID
$ollama_cmd &
SERVER_PID=$!

# Wait until Ollama HTTP API is reachable
for i in {1..60}; do
  if curl -sf http://localhost:11434/api/tags >/dev/null; then
    break
  fi
  echo "[entrypoint] Waiting for Ollama server..."
  sleep 1
done

# Ensure models exist
ensure_model() {
  local MODEL="$1"
  [ -z "$MODEL" ] && return
  if ! ollama show "$MODEL" >/dev/null 2>&1; then
    echo "[entrypoint] Pulling model $MODEL..."
    ollama pull "$MODEL"
  else
    echo "[entrypoint] Model $MODEL already present."
  fi
}

[ -n "$OLLAMA_EMBD_MODEL_NAME" ] && ensure_model "$OLLAMA_EMBD_MODEL_NAME"
[ -n "$OLLAMA_FAST_MODEL_NAME" ] && ensure_model "$OLLAMA_FAST_MODEL_NAME"
[ -n "$OLLAMA_DEEP_MODEL_NAME" ] && ensure_model "$OLLAMA_DEEP_MODEL_NAME"

# Warmup models once
for MODEL in "$OLLAMA_EMBD_MODEL_NAME" "$OLLAMA_FAST_MODEL_NAME" "$OLLAMA_DEEP_MODEL_NAME"; do
  [ -n "$MODEL" ] && ollama run "$MODEL" "warmup" >/dev/null 2>&1 &
done
wait
echo "[entrypoint] Models loaded in RAM."

# Forward signals to Ollama for clean shutdown
trap "kill -TERM $SERVER_PID 2>/dev/null" SIGTERM SIGINT

# Wait for Ollama process to keep container alive
wait $SERVER_PID
