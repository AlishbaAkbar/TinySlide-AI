# TinySlide AI

TinySlide AI is a lightweight offline text-to-slide generation system.

## Features

- Converts raw notes into presentation slides
- Generates structured JSON output
- Provides slide preview
- Works completely offline

## Model Performance

| Metric | Result |
|---|---|
| Model | TF-IDF + Logistic Regression |
| Model Size | 0.0377 MB |
| Synthetic Validation Accuracy | 100% |
| Real-World Test Accuracy | 100% |
| Average Latency | 1.2539 ms |
| Offline Support | Yes |
| API Calls | None |

## Tech Stack

- Python
- Streamlit

## Run

pip install -r requirements.txt

streamlit run app.py

