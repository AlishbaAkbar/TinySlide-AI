import pandas as pd
import joblib

model = joblib.load("slide_classifier.pkl")

df = pd.read_csv("real_world_test.csv")

correct = 0

print("TinySlide AI Real-World Evaluation")
print("=================================\n")

for _, row in df.iterrows():

    text = row["text"]
    expected = row["expected"]

    prediction = model.predict([text])[0]

    print(f"Text: {text}")
    print(f"Expected: {expected}")
    print(f"Predicted: {prediction}")
    print("-" * 50)

    if prediction == expected:
        correct += 1

accuracy = (correct / len(df)) * 100

print(f"\nReal-World Accuracy: {accuracy:.2f}%")