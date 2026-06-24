# TinySlide AI Android Offline App Deployment

This project now includes a native Android sample app in:

```text
android_app/
```

The Android app is the assignment-aligned embedded deployment target. It bundles
the ONNX model directly inside the APK and runs inference on the phone with no
internet connection and no API calls.

## What Runs On The Phone

- `android_app/app/src/main/assets/slide_classifier.onnx`
- `android_app/app/src/main/assets/slide_classifier.metadata.json`
- ONNX Runtime Android
- Local Java slide-generation logic

The app does not request internet permission in `AndroidManifest.xml`.

## Build Requirements

- Android Studio
- Android SDK platform 36, or adjust `compileSdk` in `android_app/app/build.gradle`
- A connected Android phone or emulator

## Build APK In Android Studio

1. Open Android Studio.
2. Select **Open**.
3. Choose:

   ```text
   C:\Users\Hp\Desktop\ASG\android_app
   ```

4. Let Gradle sync.
5. Connect your Android phone with USB debugging enabled.
6. Click **Run** to install and test the app.
7. For a shareable APK, use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

The APK will be generated under:

```text
android_app/app/build/outputs/apk/debug/
```

## Build APK From PowerShell

If Gradle or a Gradle wrapper is available:

```powershell
.\deploy\build_android_apk.ps1
```

This script refreshes the ONNX model assets first, then runs `assembleDebug`.
If Gradle is not installed, open the `android_app/` folder in Android Studio and
build from the menu.

## Refresh The Embedded Model

After retraining or re-exporting ONNX:

```bash
python train_model.py --export-onnx
python prepare_android_assets.py
```

This copies the latest ONNX artifacts into the Android app assets folder.

## Verify Offline Behavior

1. Install the APK on a phone.
2. Turn on airplane mode.
3. Open **TinySlide Offline**.
4. Paste notes and tap **Generate Slides**.
5. Confirm JSON slide output appears.

Because the model is packaged inside the APK and no internet permission is
declared, inference is local to the device.

## Assignment Mapping

| Requirement | Android App Evidence |
|---|---|
| Model under 50 MB | ONNX model is about 29 KB |
| Offline inference | Model is bundled in APK assets |
| Zero API calls | No internet permission declared |
| Embedded app use | Native Android app loads ONNX locally |
| Output demo app | Buildable APK project in `android_app/` |
| Latency target | Existing ONNX desktop latency is under 2 seconds; phone test should be measured on-device |

## Notes

The Android app uses the ONNX model for general classification and the same
rule-based topic routing used in the Streamlit app for practical slide headings
such as Solution, Architecture, Features, Implementation, Strategy, and Benefits.
