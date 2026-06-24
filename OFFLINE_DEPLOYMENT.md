# TinySlide AI Offline Deployment

TinySlide AI can run without network access after its Python dependencies and
model artifacts have been prepared on a connected machine.

## Recommended Environment

- Python 3.10, 3.11, or 3.12 for best compatibility with ONNX Runtime and
  sklearn conversion packages
- No API keys or cloud services are required
- CPU inference is used by default

## 1. Install Export Dependencies

On a connected development machine:

```bash
python -m venv .venv
.venv\Scripts\activate
python -m pip install -r requirements-export.txt
```

On macOS or Linux:

```bash
python -m venv .venv
. .venv/bin/activate
python -m pip install -r requirements-export.txt
```

## 2. Train Or Reuse The Pickle Model

To retrain the current classifier:

```bash
python train_model.py
```

To retrain and immediately export ONNX:

```bash
python train_model.py --export-onnx
```

If `slide_classifier.pkl` already exists and you only need ONNX:

```bash
python export_onnx.py
```

This writes:

- `models/slide_classifier.onnx`
- `models/slide_classifier.metadata.json`

## 3. Verify Inference

Use the default automatic backend:

```bash
python test_model.py
python latency_test.py
```

Force ONNX Runtime:

```bash
set TINYSLIDE_MODEL_BACKEND=onnx
python test_model.py
```

PowerShell:

```powershell
$env:TINYSLIDE_MODEL_BACKEND = "onnx"
python test_model.py
```

Use `TINYSLIDE_MODEL_BACKEND=pickle` to force the original sklearn pickle
fallback.

## 4. Prepare The Offline Bundle

```bash
python prepare_offline_bundle.py --zip
```

The bundle is written to:

```text
dist/tinyslide-ai-offline
dist/tinyslide-ai-offline.zip
```

The bundle includes the app, model loader, pickle fallback, ONNX artifacts when
present, deployment scripts, samples, dataset, requirements, and a manifest.

## 5. Prepare Offline Python Wheels

On a connected machine, download the wheels:

```bash
python -m pip download -r requirements-offline.txt -d wheelhouse
```

Copy both the offline bundle and `wheelhouse/` to the offline target machine.

## 6. Install And Run Offline

Inside the copied bundle on the offline machine:

```bash
python -m venv .venv
.venv\Scripts\activate
python -m pip install --no-index --find-links ..\wheelhouse -r requirements-offline.txt
.\deploy\run_app.ps1
```

On macOS or Linux:

```bash
python -m venv .venv
. .venv/bin/activate
python -m pip install --no-index --find-links ../wheelhouse -r requirements-offline.txt
sh deploy/run_app.sh
```

The app will use ONNX automatically when `models/slide_classifier.onnx` and
ONNX Runtime are available. Otherwise it falls back to `slide_classifier.pkl`.

## Command Line Prediction

```bash
python deploy/predict_once.py "The proposed solution automates slide generation."
python deploy/predict_once.py --backend onnx "The proposed solution automates slide generation."
```

## Deployment Files

- `export_onnx.py` converts the sklearn pipeline to ONNX.
- `slide_classifier.py` selects ONNX or pickle inference.
- `prepare_offline_bundle.py` creates the deployable directory and optional zip.
- `requirements-runtime.txt` contains app runtime dependencies.
- `requirements-export.txt` contains conversion and validation dependencies.
- `requirements-offline.txt` is used for air-gapped runtime wheel downloads.
- `deploy/run_app.ps1` and `deploy/run_app.sh` start Streamlit offline.
- `deploy/predict_once.py` provides a small CLI smoke test.
