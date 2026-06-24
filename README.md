# TinySlide AI

TinySlide AI is a lightweight offline text-to-slide generation system.

## Features

- Converts raw notes into presentation slides
- Generates structured JSON output
- Provides slide preview
- Works completely offline
- Supports ONNX Runtime inference with pickle fallback
- Packages an offline deployment bundle

## Model Performance

| Metric | Result |
|---|---|
| Model | TF-IDF + Logistic Regression |
| Model Size | 0.0377 MB |
| Synthetic Validation Accuracy | 100% |
| Real-World Test Accuracy | 100% |
| Average Latency | 1.2539 ms |
| Offline Support | Yes |
| API Calls | None |

## Tech Stack

- Python
- Streamlit
- scikit-learn
- ONNX Runtime

## Install

```bash
pip install -r requirements.txt
```

For deployment-only installs, use:

```bash
pip install -r requirements-runtime.txt
```

## Run

```bash
streamlit run app.py
```

## Train

```bash
python generate_dataset.py
python train_model.py
```

Train and export ONNX in one step:

```bash
python train_model.py --export-onnx
```

## Export ONNX

Export the existing `slide_classifier.pkl` model:

```bash
python export_onnx.py
```

Generated artifacts:

- `models/slide_classifier.onnx`
- `models/slide_classifier.metadata.json`

The app and validation scripts use `TINYSLIDE_MODEL_BACKEND=auto` by default:
ONNX is used when available, and the pickle model is used as a fallback.

Force a backend:

```bash
set TINYSLIDE_MODEL_BACKEND=onnx
python test_model.py
```

PowerShell:

```powershell
$env:TINYSLIDE_MODEL_BACKEND = "pickle"
python test_model.py
```

## Offline Deployment

Prepare a deployable bundle:

```bash
python prepare_offline_bundle.py --zip
```

See [OFFLINE_DEPLOYMENT.md](OFFLINE_DEPLOYMENT.md) for the full air-gapped
deployment workflow, including wheel downloads and launch scripts.

## Android Offline App

The embedded mobile app target is in:

```text
android_app/
```

It bundles `models/slide_classifier.onnx` inside the APK assets and runs
inference locally on Android with no internet permission.

Refresh Android model assets after export:

```bash
python prepare_android_assets.py
```

Build from PowerShell when Gradle is available:

```powershell
.\deploy\build_android_apk.ps1
```

See [ANDROID_APP_DEPLOYMENT.md](ANDROID_APP_DEPLOYMENT.md) for APK build and
offline phone-testing steps.

## Assignment Documentation

See [ASSIGNMENT_DOCUMENTATION.md](ASSIGNMENT_DOCUMENTATION.md) for the complete
assignment-ready report, including objectives, methodology, results, testing,
deployment, limitations, and conclusion.
