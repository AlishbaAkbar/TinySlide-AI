$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir "..")

$env:TINYSLIDE_MODEL_BACKEND = "auto"
streamlit run (Join-Path $ProjectRoot "app.py")
