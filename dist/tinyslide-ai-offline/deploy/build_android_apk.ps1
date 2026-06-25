$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$AndroidRoot = Join-Path $ProjectRoot "android_app"

Set-Location $ProjectRoot
python prepare_android_assets.py

Set-Location $AndroidRoot

if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat assembleDebug
}
elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    gradle assembleDebug
}
else {
    throw "Gradle was not found. Open android_app in Android Studio and use Build > Build Bundle(s) / APK(s) > Build APK(s), or add a Gradle wrapper."
}

Write-Host "APK output: android_app\app\build\outputs\apk\debug\app-debug.apk"
