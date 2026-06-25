import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from slide_classifier import load_classifier


def main():
    parser = argparse.ArgumentParser(description="Run one TinySlide AI prediction.")
    parser.add_argument("text", help="Text to classify.")
    parser.add_argument(
        "--backend",
        choices=["auto", "onnx", "pickle"],
        default="auto",
        help="Inference backend to use.",
    )
    args = parser.parse_args()

    model = load_classifier(backend=args.backend)
    prediction = model.predict([args.text])[0]

    print(f"Backend: {model.backend}")
    print(f"Prediction: {prediction}")


if __name__ == "__main__":
    main()
