import argparse
import shutil
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parent
DEFAULT_OUTPUT_DIR = PROJECT_ROOT / "dist" / "tinyslide-ai-offline"

REQUIRED_FILES = [
    "app.py",
    "slide_classifier.py",
    "slide_classifier.pkl",
    "README.md",
    "OFFLINE_DEPLOYMENT.md",
    "requirements-offline.txt",
    "requirements-runtime.txt",
    "test_model.py",
    "latency_test.py",
    "real_world_test.csv",
]

DEVELOPER_FILES = [
    "train_model.py",
    "export_onnx.py",
    "prepare_android_assets.py",
    "generate_dataset.py",
    "evaluate_real_world.py",
    "requirements.txt",
    "requirements-export.txt",
    "model_evaluation_report.txt",
]

DIRECTORIES = [
    "dataset",
    "deploy",
    "android_app",
    "models",
    "samples",
]


def _is_relative_to(path, parent):
    try:
        path.relative_to(parent)
    except ValueError:
        return False

    return True


def _copy_file(relative_path, output_dir, copied, missing):
    source = PROJECT_ROOT / relative_path
    destination = output_dir / relative_path

    if not source.exists():
        missing.append(relative_path)
        return

    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, destination)
    copied.append(relative_path)


def _copy_directory(relative_path, output_dir, copied, missing):
    source = PROJECT_ROOT / relative_path
    destination = output_dir / relative_path

    if not source.exists():
        missing.append(relative_path)
        return

    shutil.copytree(
        source,
        destination,
        dirs_exist_ok=True,
        ignore=shutil.ignore_patterns("__pycache__", "*.pyc"),
    )

    for copied_file in destination.rglob("*"):
        if copied_file.is_file():
            copied.append(str(copied_file.relative_to(output_dir)))


def _write_manifest(output_dir, copied, missing):
    manifest = output_dir / "MANIFEST.txt"
    lines = [
        "TinySlide AI Offline Bundle",
        "===========================",
        "",
        "Included files:",
    ]
    lines.extend(f"- {path}" for path in sorted(copied))

    if missing:
        lines.extend(["", "Missing optional or required files:"])
        lines.extend(f"- {path}" for path in sorted(set(missing)))

    manifest.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return manifest


def prepare_bundle(output_dir=DEFAULT_OUTPUT_DIR, include_developer_files=True, make_zip=False):
    output_dir = Path(output_dir)
    copied = []
    missing = []

    if output_dir.exists():
        resolved_output = output_dir.resolve()
        resolved_dist = (PROJECT_ROOT / "dist").resolve()

        if not _is_relative_to(resolved_output, resolved_dist):
            raise ValueError(
                "Refusing to clean an existing output directory outside dist/."
            )

        shutil.rmtree(output_dir)

    output_dir.mkdir(parents=True, exist_ok=True)

    files = REQUIRED_FILES[:]
    if include_developer_files:
        files.extend(DEVELOPER_FILES)

    for relative_path in files:
        _copy_file(relative_path, output_dir, copied, missing)

    for relative_path in DIRECTORIES:
        _copy_directory(relative_path, output_dir, copied, missing)

    manifest = _write_manifest(output_dir, copied, missing)

    if not (output_dir / "models" / "slide_classifier.onnx").exists():
        print(
            "Warning: ONNX model was not found in the bundle. "
            "Run `python export_onnx.py` before packaging to include it."
        )

    print(f"Offline bundle prepared at {output_dir}")
    print(f"Manifest written to {manifest}")

    if make_zip:
        zip_path = shutil.make_archive(str(output_dir), "zip", root_dir=output_dir)
        print(f"Zip archive written to {zip_path}")

    return output_dir


def main():
    parser = argparse.ArgumentParser(
        description="Prepare a self-contained TinySlide AI offline deployment bundle."
    )
    parser.add_argument(
        "--output",
        default=str(DEFAULT_OUTPUT_DIR),
        help="Output directory for the offline bundle.",
    )
    parser.add_argument(
        "--runtime-only",
        action="store_true",
        help="Exclude training, export, and evaluation helper files.",
    )
    parser.add_argument(
        "--zip",
        action="store_true",
        help="Also create a zip archive next to the bundle directory.",
    )
    args = parser.parse_args()

    prepare_bundle(
        output_dir=args.output,
        include_developer_files=not args.runtime_only,
        make_zip=args.zip,
    )


if __name__ == "__main__":
    main()
