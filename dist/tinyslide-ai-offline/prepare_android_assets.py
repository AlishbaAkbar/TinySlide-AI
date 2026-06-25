import shutil
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parent
ASSETS_DIR = PROJECT_ROOT / "android_app" / "app" / "src" / "main" / "assets"
MODEL_FILES = [
    PROJECT_ROOT / "models" / "slide_classifier.onnx",
    PROJECT_ROOT / "models" / "slide_classifier.metadata.json",
]


def sync_android_assets():
    ASSETS_DIR.mkdir(parents=True, exist_ok=True)

    for source in MODEL_FILES:
        if not source.exists():
            raise FileNotFoundError(
                f"Missing {source}. Run `python export_onnx.py` first."
            )

        destination = ASSETS_DIR / source.name
        shutil.copy2(source, destination)
        print(f"Copied {source} -> {destination}")


if __name__ == "__main__":
    sync_android_assets()
