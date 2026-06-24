import argparse
import json
from datetime import datetime, timezone
from pathlib import Path

import joblib

from slide_classifier import OnnxSlideClassifier, SklearnSlideClassifier


DEFAULT_PICKLE_MODEL_PATH = Path("slide_classifier.pkl")
DEFAULT_ONNX_MODEL_PATH = Path("models/slide_classifier.onnx")
DEFAULT_METADATA_PATH = Path("models/slide_classifier.metadata.json")
DEFAULT_TARGET_OPSET = 15
SAMPLE_TEXTS = [
    "The system reduces manual effort and saves time.",
    "The main challenge is manual slide creation.",
    "First the user enters text and then the system processes it.",
    "The proposed solution automates content generation.",
]


def _get_classes(model):
    classifier = getattr(model, "named_steps", {}).get("classifier")
    if classifier is None:
        return []

    return [str(label) for label in getattr(classifier, "classes_", [])]


def _convert_to_onnx(model, target_opset):
    try:
        from skl2onnx import convert_sklearn
        from skl2onnx.common.data_types import StringTensorType
    except ImportError as exc:
        raise ImportError(
            "skl2onnx is required to export the classifier. "
            "Install the export dependencies with: "
            "pip install -r requirements-export.txt"
        ) from exc

    classifier = getattr(model, "named_steps", {}).get("classifier")
    options = {}
    if classifier is not None:
        options[id(classifier)] = {"zipmap": False}

    return convert_sklearn(
        model,
        initial_types=[("input", StringTensorType([None, 1]))],
        target_opset=target_opset,
        options=options,
    )


def _write_metadata(metadata_path, pickle_model_path, onnx_model_path, onnx_model, model, target_opset):
    metadata = {
        "name": "TinySlide AI slide classifier",
        "source_model": str(pickle_model_path),
        "onnx_model": str(onnx_model_path),
        "backend": "onnxruntime",
        "target_opset": target_opset,
        "classes": _get_classes(model),
        "input_names": [input_value.name for input_value in onnx_model.graph.input],
        "output_names": [output_value.name for output_value in onnx_model.graph.output],
        "generated_at": datetime.now(timezone.utc).isoformat(timespec="seconds"),
    }

    metadata_path.write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    return metadata


def _check_parity(pickle_model_path, onnx_model_path, sample_texts):
    sklearn_model = SklearnSlideClassifier(pickle_model_path)
    onnx_model = OnnxSlideClassifier(onnx_model_path)

    sklearn_predictions = [str(value) for value in sklearn_model.predict(sample_texts)]
    onnx_predictions = [str(value) for value in onnx_model.predict(sample_texts)]

    if sklearn_predictions != onnx_predictions:
        raise RuntimeError(
            "ONNX parity check failed: "
            f"pickle={sklearn_predictions}, onnx={onnx_predictions}"
        )

    return onnx_predictions


def export_onnx_model(
    pickle_model_path=DEFAULT_PICKLE_MODEL_PATH,
    onnx_model_path=DEFAULT_ONNX_MODEL_PATH,
    metadata_path=DEFAULT_METADATA_PATH,
    target_opset=DEFAULT_TARGET_OPSET,
    check=True,
    sample_texts=None,
):
    pickle_model_path = Path(pickle_model_path)
    onnx_model_path = Path(onnx_model_path)
    metadata_path = Path(metadata_path)
    sample_texts = sample_texts or SAMPLE_TEXTS

    model = joblib.load(pickle_model_path)
    onnx_model = _convert_to_onnx(model, target_opset)

    onnx_model_path.parent.mkdir(parents=True, exist_ok=True)
    metadata_path.parent.mkdir(parents=True, exist_ok=True)
    onnx_model_path.write_bytes(onnx_model.SerializeToString())
    metadata = _write_metadata(
        metadata_path,
        pickle_model_path,
        onnx_model_path,
        onnx_model,
        model,
        target_opset,
    )

    print(f"ONNX model exported to {onnx_model_path}")
    print(f"Metadata written to {metadata_path}")
    print(f"Inputs: {', '.join(metadata['input_names'])}")
    print(f"Outputs: {', '.join(metadata['output_names'])}")

    if check:
        try:
            predictions = _check_parity(pickle_model_path, onnx_model_path, sample_texts)
        except ImportError as exc:
            print(f"Parity check skipped: {exc}")
        else:
            print("Parity check passed")
            for text, prediction in zip(sample_texts, predictions):
                print(f"- {prediction}: {text}")

    return onnx_model_path


def main():
    parser = argparse.ArgumentParser(
        description="Export TinySlide AI's sklearn pipeline to ONNX."
    )
    parser.add_argument(
        "--pickle-model",
        default=str(DEFAULT_PICKLE_MODEL_PATH),
        help="Path to the trained pickle model.",
    )
    parser.add_argument(
        "--onnx-output",
        default=str(DEFAULT_ONNX_MODEL_PATH),
        help="Path where the ONNX model should be written.",
    )
    parser.add_argument(
        "--metadata-output",
        default=str(DEFAULT_METADATA_PATH),
        help="Path where ONNX metadata should be written.",
    )
    parser.add_argument(
        "--target-opset",
        type=int,
        default=DEFAULT_TARGET_OPSET,
        help="ONNX target opset.",
    )
    parser.add_argument(
        "--no-check",
        action="store_true",
        help="Skip ONNX Runtime parity validation after export.",
    )
    args = parser.parse_args()

    export_onnx_model(
        pickle_model_path=args.pickle_model,
        onnx_model_path=args.onnx_output,
        metadata_path=args.metadata_output,
        target_opset=args.target_opset,
        check=not args.no_check,
    )


if __name__ == "__main__":
    main()
