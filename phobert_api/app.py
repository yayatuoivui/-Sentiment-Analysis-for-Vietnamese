from fastapi import FastAPI
from pydantic import BaseModel
from transformers import RobertaForSequenceClassification, AutoTokenizer
import torch

app = FastAPI()

# Load the PhoBERT model and tokenizer
model_name = "wonrax/phobert-base-vietnamese-sentiment"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = RobertaForSequenceClassification.from_pretrained(model_name)

# Define input schema
class TextInput(BaseModel):
    text: str

# Sentiment labels mapping
label_map = {0: "negative", 1: "positive", 2: "neutral"}

@app.post("/predict")
async def predict_sentiment(input: TextInput):
    # Tokenize input text
    inputs = tokenizer(input.text, return_tensors="pt", truncation=True, padding=True, max_length=256)
    
    # Perform inference
    with torch.no_grad():
        outputs = model(**inputs)
        logits = outputs.logits
        predicted_class = torch.argmax(logits, dim=1).item()
    
    # Map prediction to sentiment label
    sentiment = label_map[predicted_class]
    return {"sentiment": sentiment}

@app.get("/")
async def root():
    return {"message": "PhoBERT Sentiment Analysis API"}