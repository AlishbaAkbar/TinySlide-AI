from slide_classifier import load_classifier

model = load_classifier()

samples = [
    "The system reduces manual effort and saves time.",
    "The main challenge is manual slide creation.",
    "First the user enters text and then the system processes it.",
    "The proposed solution automates content generation."
]

for text in samples:
    prediction = model.predict([text])[0]

    print("\nText:", text)
    print("Class:", prediction)
