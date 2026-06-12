import csv
import random

classes = {
    "definition": [
        "{} is a system that helps users understand complex information.",
        "{} refers to a method used to organize and process data.",
        "{} is a technique designed to improve productivity.",
        "{} means using technology to solve real-world problems.",
        "{} is an approach that converts raw input into useful output."
    ],
    "problem": [
        "The main problem is that users spend too much time on manual work.",
        "A major challenge is the lack of automation in this process.",
        "Many users face difficulty while organizing large amounts of content.",
        "The current system is slow, repetitive, and time-consuming.",
        "One key issue is that manual formatting reduces productivity."
    ],
    "solution": [
        "The proposed solution automates the content structuring process.",
        "This system solves the issue by generating organized output automatically.",
        "Our solution uses a lightweight model to classify and structure text.",
        "The application provides an offline way to create formatted content.",
        "This approach improves efficiency by reducing manual effort."
    ],
    "benefit": [
        "This improves speed, accuracy, and user productivity.",
        "It helps users complete their work faster and more efficiently.",
        "The system reduces manual effort and saves time.",
        "Users benefit from offline access and better privacy.",
        "This makes the workflow faster, cheaper, and more reliable."
    ],
    "process": [
        "First, the user enters raw text into the application.",
        "Next, the system analyzes the text and detects its content type.",
        "Then, the model classifies the sentence into a suitable category.",
        "After that, the app generates structured slide content.",
        "Finally, the output is exported as JSON or PowerPoint."
    ],
    "comparison": [
        "Compared to cloud tools, this system works without internet access.",
        "Unlike manual formatting, the app generates structure automatically.",
        "Traditional tools require more effort, while this solution is faster.",
        "Cloud models are powerful, but offline models provide better privacy.",
        "This method is smaller and faster than large language models."
    ],
    "example": [
        "For example, a student can paste lecture notes and generate slides.",
        "For instance, a business owner can convert product details into a pitch deck.",
        "An example use case is creating presentation slides from meeting notes.",
        "For example, teachers can prepare classroom slides from lesson content.",
        "For instance, researchers can summarize notes into structured sections."
    ],
    "statistic": [
        "The model achieved over 90 percent accuracy on the test dataset.",
        "The final model size is less than 50 MB.",
        "The system generates output in under 2 seconds.",
        "Testing was performed on a held-out dataset of sample inputs.",
        "The offline model reduces server cost by 100 percent."
    ]
}

topics = [
    "Artificial Intelligence",
    "Machine Learning",
    "Online Education",
    "Smart Healthcare",
    "E-commerce Automation",
    "Digital Marketing",
    "Transport Management",
    "Text-to-Slide Generation",
    "Offline AI",
    "Content Generation"
]

rows = []

for label, templates in classes.items():
    for _ in range(150):
        template = random.choice(templates)

        if "{}" in template:
            text = template.format(random.choice(topics))
        else:
            text = template

        rows.append([text, label])

random.shuffle(rows)

with open("slide_training_data.csv", "w", newline="", encoding="utf-8") as file:
    writer = csv.writer(file)
    writer.writerow(["text", "label"])
    writer.writerows(rows)

print("Dataset generated successfully!")
print("Total rows:", len(rows))