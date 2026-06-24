import time
from slide_classifier import load_classifier

model = load_classifier()

sample_text = "The system reduces manual effort and saves time."

start_time = time.time()

for _ in range(100):
    model.predict([sample_text])

end_time = time.time()

average_latency = (end_time - start_time) / 100
average_latency_ms = average_latency * 1000

print("TinySlide AI Latency Test")
print("=========================")
print(f"Backend: {model.backend}")
print(f"Average Prediction Latency: {average_latency_ms:.4f} ms")
print(f"Requirement: < 2000 ms")
