# Product Embedding Server

FastAPI server for creating query embeddings with the same model used by `embedding-batch/generate_embeddings.py`.

## Model

```text
sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2
```

The model is loaded once when the server starts.

## Setup

```powershell
cd embedding-server
pip install -r requirements.txt
```

## Run

```powershell
uvicorn main:app --host 0.0.0.0 --port 8000
```

## API

```http
POST /embed
Content-Type: application/json
```

```json
{
  "text": "가볍고 배터리 오래가는 노트북"
}
```

```json
{
  "model": "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
  "embedding": [0.123, -0.456]
}
```
