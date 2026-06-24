#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)

export TINYSLIDE_MODEL_BACKEND=auto
streamlit run "$PROJECT_ROOT/app.py"
