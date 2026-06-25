import os
from pathlib import Path

import joblib


PROJECT_ROOT = Path(__file__).resolve().parent
DEFAULT_PICKLE_MODEL_PATH = PROJECT_ROOT / "slide_classifier.pkl"
DEFAULT_ONNX_MODEL_PATH = PROJECT_ROOT / "models" / "slide_classifier.onnx"


def _normalize_texts(texts):
    if isinstance(texts, str):
        return [texts]

    return [str(text) for text in texts]


class SklearnSlideClassifier:
    backend = "pickle"

    def __init__(self, model_path=DEFAULT_PICKLE_MODEL_PATH):
        self.model_path = Path(model_path)
        self.model = joblib.load(self.model_path)

    def predict(self, texts):
        return self.model.predict(_normalize_texts(texts))


class OnnxSlideClassifier:
    backend = "onnx"

    def __init__(self, model_path=DEFAULT_ONNX_MODEL_PATH):
        self.model_path = Path(model_path)

        try:
            import onnxruntime as ort
        except ImportError as exc:
            raise ImportError(
                "onnxruntime is required to use the ONNX backend. "
                "Install the runtime dependencies or use the pickle backend."
            ) from exc

        self.session = ort.InferenceSession(
            str(self.model_path),
            providers=["CPUExecutionProvider"],
        )
        self.input_name = self.session.get_inputs()[0].name

    def predict(self, texts):
        try:
            import numpy as np
        except ImportError as exc:
            raise ImportError("numpy is required to run ONNX inference.") from exc

        normalized_texts = _normalize_texts(texts)
        inputs = np.asarray(normalized_texts, dtype=np.object_).reshape((-1, 1))
        outputs = self.session.run(None, {self.input_name: inputs})

        for output in outputs:
            values = np.asarray(output)
            if values.shape[0] != len(normalized_texts):
                continue
            if values.dtype.kind in {"O", "S", "U"}:
                return values.reshape(-1).astype(str)

        return np.asarray(outputs[0]).reshape(-1).astype(str)


def load_classifier(
    backend=None,
    onnx_model_path=DEFAULT_ONNX_MODEL_PATH,
    pickle_model_path=DEFAULT_PICKLE_MODEL_PATH,
):
    selected_backend = (backend or os.getenv("TINYSLIDE_MODEL_BACKEND", "auto")).lower()

    if selected_backend not in {"auto", "onnx", "pickle"}:
        raise ValueError(
            "Unsupported model backend. Use 'auto', 'onnx', or 'pickle'."
        )

    if selected_backend in {"auto", "onnx"}:
        if Path(onnx_model_path).exists():
            try:
                return OnnxSlideClassifier(onnx_model_path)
            except Exception:
                if selected_backend == "onnx":
                    raise
        elif selected_backend == "onnx":
            raise FileNotFoundError(f"ONNX model not found: {onnx_model_path}")

    return SklearnSlideClassifier(pickle_model_path)
