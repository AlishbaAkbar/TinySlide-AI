import pandas as pd
import joblib
import os
import time

start_time = time.time()
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report


df = pd.read_csv("dataset/slide_training_data.csv")

X = df["text"]
y = df["label"]

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.2,
    random_state=42,
    stratify=y
)

model = Pipeline([
    ("tfidf", TfidfVectorizer(
        lowercase=True,
        stop_words="english",
        ngram_range=(1, 2)
    )),
    ("classifier", LogisticRegression(
        max_iter=1000
    ))
])

model.fit(X_train, y_train)

predictions = model.predict(X_test)

accuracy = accuracy_score(y_test, predictions)

print("Model Training Completed")
print("Accuracy:", round(accuracy * 100, 2), "%")
print("\nClassification Report:")
print(classification_report(y_test, predictions))
report = classification_report(y_test, predictions)

with open("model_evaluation_report.txt", "w") as file:
    file.write("TinySlide AI Model Evaluation Report\n")
    file.write("====================================\n\n")
    file.write(f"Accuracy: {round(accuracy * 100, 2)}%\n\n")
    file.write("Classification Report:\n")
    file.write(report)

joblib.dump(model, "slide_classifier.pkl")

print("\nModel saved as slide_classifier.pkl")


size_mb = os.path.getsize("slide_classifier.pkl") / (1024 * 1024)

print(f"\nModel Size: {size_mb:.4f} MB")
training_time = time.time() - start_time

print(f"Training Time: {training_time:.2f} seconds")