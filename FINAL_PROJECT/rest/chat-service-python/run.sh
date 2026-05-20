#!/usr/bin/env bash
# Lance ChatService REST Python (Flask) en local.
set -e
cd "$(dirname "$0")"

if [ ! -d ".venv" ]; then
    echo "[setup] Creation du venv et installation des dependances..."
    python3 -m venv .venv
    .venv/bin/pip install --upgrade pip
    .venv/bin/pip install -r requirements.txt
fi

echo "[run] Lancement ChatService REST sur :${SERVICE_PORT:-8082}"
exec .venv/bin/python app.py
