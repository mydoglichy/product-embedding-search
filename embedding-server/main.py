from contextlib import asynccontextmanager
from typing import List

from fastapi import FastAPI
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer


MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
model: SentenceTransformer | None = None


class EmbedRequest(BaseModel):
    text: str = Field(..., min_length=1)


class EmbedResponse(BaseModel):
    model: str
    embedding: List[float]


@asynccontextmanager
async def lifespan(app: FastAPI):
    global model
    model = SentenceTransformer(MODEL_NAME)
    yield


app = FastAPI(title="Product Embedding Server", lifespan=lifespan)


@app.post("/embed", response_model=EmbedResponse)
def embed(request: EmbedRequest) -> EmbedResponse:
    if model is None:
        raise RuntimeError("Embedding model is not loaded.")

    embedding = model.encode(request.text, convert_to_numpy=True).astype(float).tolist()
    return EmbedResponse(model=MODEL_NAME, embedding=embedding)

