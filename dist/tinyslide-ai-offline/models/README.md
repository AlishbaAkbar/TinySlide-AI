# Model Artifacts

This directory stores deployment-ready model artifacts generated from the
trained TinySlide AI classifier.

Expected generated files:

- `slide_classifier.onnx` - ONNX Runtime model exported from `slide_classifier.pkl`
- `slide_classifier.metadata.json` - export metadata, classes, inputs, and outputs

Generate them with:

```bash
python export_onnx.py
```

If ONNX Runtime is not installed in the target environment, TinySlide AI can
still run from the original `slide_classifier.pkl` fallback.
