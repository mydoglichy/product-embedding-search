# Local Product Embedding Batch

이 배치는 외부 유료 API를 사용하지 않고 로컬 `sentence-transformers` 모델로 상품 임베딩을 생성합니다.

Spring 서버에서 Python을 실행하지 않습니다. 사람이 터미널에서 직접 `generate_embeddings.py`를 실행하면, MySQL의 `products` 테이블을 읽고 아직 임베딩이 없는 상품만 `product_embeddings` 테이블에 저장합니다.

## Model

```text
sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2
```

위 모델명이 `product_embeddings.model` 컬럼에 저장됩니다.

## Setup

```powershell
cd embedding-batch
pip install -r requirements.txt
```

기본적으로 배치는 `../backend/.env`를 읽습니다. Spring에서 쓰는 DB 설정을 그대로 공유합니다.

지원하는 Spring 환경 변수:

```properties
MYSQL_DATABASE=product_embedding_search
MYSQL_USERNAME=product_user
MYSQL_PASSWORD=your_password
```

`MYSQL_URL`이 있으면 host, port, database도 읽습니다.

배치만 별도 DB 설정으로 실행하고 싶으면 `.env.example`을 복사해서 `embedding-batch/.env`를 만들 수 있습니다. 이 파일은 `backend/.env`보다 우선 적용됩니다.

```powershell
cp .env.example .env
```

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=product_embedding_search
DB_USER=product_user
DB_PASSWORD=your_password
```

## Run

```powershell
python generate_embeddings.py
```

실행 예:

```text
total products: 50
created embeddings: 48
skipped products: 2
model: sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2
```

실행 후 `product_embeddings.embedding_json`에 임베딩 벡터가 JSON 배열 문자열로 저장됩니다.

## Flow

```text
Python 배치로 임베딩 생성
  -> MySQL product_embeddings 저장
  -> Spring 검색 API에서 product_embeddings를 읽어 유사도 계산
```

현재 구조에서 Spring은 배치 실행을 담당하지 않습니다. 검색 API를 추가할 때 저장된 임베딩을 읽어 cosine similarity 등을 계산하는 역할만 맡습니다.
