# TinySlide AI Assignment Documentation

## Project Title

TinySlide AI: Offline Text-to-Slide Generation with ONNX Deployment Support

## Abstract

TinySlide AI is a lightweight offline application that converts raw notes into
structured presentation slide content. The system uses a TF-IDF vectorizer and
Logistic Regression classifier to identify the type of each sentence, group
related content, and generate JSON and PowerPoint output. The project has been
extended with ONNX export support and offline deployment preparation so it can
run without cloud APIs or internet access.

## Problem Statement

Creating presentation slides manually is time-consuming, repetitive, and often
requires users to organize unstructured notes into suitable slide sections. Many
AI presentation tools depend on cloud services, which can create privacy,
latency, and availability concerns. This project solves that problem by
providing a small, fast, offline text-to-slide generator.

## Objectives

- Convert raw text notes into structured slide content.
- Classify sentences into useful slide categories.
- Generate downloadable JSON and PowerPoint files.
- Keep inference lightweight and fully offline.
- Export the trained sklearn model to ONNX for deployment.
- Prepare an offline-ready bundle with model artifacts and launch scripts.
- Preserve compatibility with the original pickle model.

## Scope

The system focuses on sentence-level classification and slide organization. It
does not use large language models, external APIs, or cloud inference. The
generated slides are simple but structured, making the project suitable for
offline educational, academic, and prototype use cases.

## Dataset

The training dataset is stored in:

```text
dataset/slide_training_data.csv
```

Each row contains:

- `text`: a sample sentence.
- `label`: the expected content category.

The supported labels are:

- `definition`
- `problem`
- `solution`
- `benefit`
- `process`
- `comparison`
- `example`
- `statistic`

The dataset can be regenerated with:

```bash
python generate_dataset.py
```

## Model Methodology

TinySlide AI uses a classic machine learning pipeline:

```text
Raw Text -> TF-IDF Vectorizer -> Logistic Regression -> Content Type
```

The TF-IDF vectorizer converts text into numerical features. Logistic Regression
then predicts the most likely content type for each sentence. This approach was
selected because it is small, fast, interpretable, and suitable for offline
deployment.

## Training Process

Training is handled by:

```text
train_model.py
```

The script:

1. Loads the dataset.
2. Splits it into training and testing data.
3. Trains a TF-IDF + Logistic Regression pipeline.
4. Prints accuracy and classification metrics.
5. Saves the model as `slide_classifier.pkl`.
6. Optionally exports ONNX using `--export-onnx`.

Training command:

```bash
python train_model.py
```

Training with ONNX export:

```bash
python train_model.py --export-onnx
```

## ONNX Export

ONNX export is implemented in:

```text
export_onnx.py
```

The exporter converts the sklearn pipeline into:

```text
models/slide_classifier.onnx
models/slide_classifier.metadata.json
```

The metadata file records the model name, class labels, input names, output
names, backend, target opset, and generation time.

Export command:

```bash
python export_onnx.py
```

After export, the script performs a parity check to confirm that ONNX
predictions match the original pickle model on sample inputs.

## Inference Backends

Inference is centralized in:

```text
slide_classifier.py
```

The project supports three backend modes:

- `auto`: uses ONNX when available, otherwise falls back to pickle.
- `onnx`: forces ONNX Runtime inference.
- `pickle`: forces the original sklearn pickle model.

Backend selection is controlled by:

```text
TINYSLIDE_MODEL_BACKEND
```

Example:

```powershell
$env:TINYSLIDE_MODEL_BACKEND = "onnx"
python test_model.py
```

## Application Workflow

The main Streamlit app is:

```text
app.py
```

Workflow:

1. User enters raw notes.
2. Notes are split into clean sentences.
3. Each sentence is classified by the model.
4. Sentences are grouped by content type.
5. Slide headings and layouts are suggested.
6. The user can preview the slide structure.
7. The user can download JSON or PowerPoint output.

Run command:

```bash
streamlit run app.py
```

## Offline Deployment

Offline deployment preparation is handled by:

```text
prepare_offline_bundle.py
```

Bundle command:

```bash
python prepare_offline_bundle.py --zip
```

Generated output:

```text
dist/tinyslide-ai-offline
dist/tinyslide-ai-offline.zip
```

The bundle includes source files, model artifacts, deployment scripts,
requirements files, sample data, and a manifest.

## Android Embedded App Deployment

The assignment also requires a working embedded app target. This project
includes a native Android sample app in:

```text
android_app/
```

The Android app bundles the exported ONNX model directly inside:

```text
android_app/app/src/main/assets/slide_classifier.onnx
```

It loads the model with ONNX Runtime Android and generates slide JSON locally on
the phone. The app does not declare internet permission, so inference is
designed to run without network access.

Android deployment documentation is provided in:

```text
ANDROID_APP_DEPLOYMENT.md
```

To refresh the embedded model after retraining:

```bash
python train_model.py --export-onnx
python prepare_android_assets.py
```

For air-gapped installation, dependencies can be downloaded into a wheelhouse:

```bash
python -m pip download -r requirements-offline.txt -d wheelhouse
```

Then install offline:

```bash
python -m pip install --no-index --find-links wheelhouse -r requirements-offline.txt
```

## Testing And Evaluation

The following scripts are used for validation:

| Script | Purpose |
|---|---|
| `test_model.py` | Runs sample prediction checks |
| `evaluate_real_world.py` | Evaluates real-world test examples |
| `latency_test.py` | Measures average prediction latency |
| `deploy/predict_once.py` | Runs a single command-line prediction |
| `export_onnx.py` | Checks pickle-to-ONNX prediction parity |
| `android_app/` | Native Android app for embedded offline inference |

Recommended test sequence:

```powershell
python test_model.py
$env:TINYSLIDE_MODEL_BACKEND = "onnx"
python evaluate_real_world.py
python latency_test.py
python deploy\predict_once.py --backend onnx "The proposed solution automates slide generation."
```

## Results

| Metric | Result |
|---|---|
| Model | TF-IDF + Logistic Regression |
| Pickle Model | `slide_classifier.pkl` |
| ONNX Model | `models/slide_classifier.onnx` |
| Real-World Accuracy | 100.00% |
| ONNX Latency | Approximately 0.136 ms in local testing |
| Offline Support | Yes |
| Cloud API Calls | None |
| Embedded App Target | Native Android APK project |

## Project File Structure

```text
TinySlide AI
├── app.py
├── slide_classifier.py
├── train_model.py
├── export_onnx.py
├── prepare_offline_bundle.py
├── prepare_android_assets.py
├── generate_dataset.py
├── test_model.py
├── evaluate_real_world.py
├── latency_test.py
├── slide_classifier.pkl
├── dataset/
│   └── slide_training_data.csv
├── models/
│   ├── slide_classifier.onnx
│   └── slide_classifier.metadata.json
├── deploy/
│   ├── predict_once.py
│   ├── run_app.ps1
│   └── run_app.sh
├── android_app/
│   └── app/
│       └── src/main/
│           ├── assets/slide_classifier.onnx
│           └── java/com/tinyslide/offline/
├── requirements.txt
├── requirements-runtime.txt
├── requirements-export.txt
├── requirements-offline.txt
├── README.md
└── OFFLINE_DEPLOYMENT.md
```

## Key Improvements Added

- Added ONNX export for deployment-friendly inference.
- Added ONNX Runtime backend with pickle fallback.
- Added backend selection through an environment variable.
- Added offline bundle generation with zip packaging.
- Added runtime, export, and offline dependency files.
- Added deployment launch scripts for Windows and Unix-like systems.
- Added a native Android app project for embedded APK deployment.
- Updated documentation for testing, export, and deployment.

## Limitations

- The classifier depends on the quality and coverage of the labeled dataset.
- Generated slides are structured but visually simple.
- The system classifies sentences rather than generating new long-form content.
- ONNX export requires export dependencies such as `skl2onnx` and `onnx`.

## Future Enhancements

- Add richer slide themes and templates.
- Add drag-and-drop file input.
- Support PDF or DOCX note ingestion.
- Add more training examples for broader classification coverage.
- Add automated unit tests with a test framework.
- Add a desktop executable packaging option.

## Conclusion

TinySlide AI demonstrates how a small machine learning model can support offline
text-to-slide generation without relying on cloud services. The ONNX export and
offline deployment workflow make the system easier to distribute, test, and run
in restricted environments while preserving the original sklearn pickle fallback.
