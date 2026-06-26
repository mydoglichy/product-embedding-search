# Product Embedding Search

Spring Boot 3 + Java 21 + React 기반 상품 검색 연습 프로젝트입니다.

현재 구현 상태는 다음과 같습니다.

- Backend: Spring Boot REST API
- Storage: MySQL
- Initial data: CSV import API
- Search: DB 기반 단순 키워드 검색
- Embedding: DB 저장 구조만 준비, OpenAI 연동은 다음 단계

## Project Structure

```text
product-embedding-search/
├── README.md
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew
│   ├── gradlew.bat
│   └── src/
└── frontend/
    └── .gitkeep
```

## Backend Stack

- Java 21
- Spring Boot 3
- Gradle
- Spring Web
- Spring Data JPA
- MySQL
- Apache Commons CSV

## Current Flow

```text
CSV 파일 준비
    ↓
POST /api/admin/products/import-csv
    ↓
CSV 데이터를 MySQL products 테이블에 저장
    ↓
GET /api/products
GET /api/products/{id}
GET /api/search?query=검색어
    ↓
MySQL에서 상품 조회/검색
```

나중에 임베딩 검색을 붙이면 흐름은 다음처럼 확장합니다.

```text
관리자 API로 상품 추가/수정
    ↓
상품의 embedding_text 기준으로 OpenAI Embedding 생성
    ↓
product_embeddings 테이블에 embedding_json 저장
    ↓
사용자 검색어를 임베딩
    ↓
DB에 저장된 상품 임베딩과 cosine similarity 계산
    ↓
유사도 높은 상품 반환
```

## MySQL에서 해야 할 일

MySQL에 접속해서 데이터베이스를 먼저 생성하세요.

```sql
CREATE DATABASE product_embedding_search
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

전용 계정을 쓰고 싶으면 다음처럼 만들 수 있습니다.

```sql
CREATE USER 'product_user'@'localhost' IDENTIFIED BY 'product_password';
GRANT ALL PRIVILEGES ON product_embedding_search.* TO 'product_user'@'localhost';
FLUSH PRIVILEGES;
```

테이블은 Spring Boot 실행 시 JPA가 자동 생성합니다.

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Backend 설정

[backend/src/main/resources/application.properties](backend/src/main/resources/application.properties)

기본값은 로컬 MySQL `root` 계정을 사용합니다.

```properties
spring.datasource.url=${MYSQL_URL:jdbc:mysql://localhost:3306/product_embedding_search?serverTimezone=Asia/Seoul&characterEncoding=UTF-8}
spring.datasource.username=${MYSQL_USERNAME:root}
spring.datasource.password=${MYSQL_PASSWORD:}
```

비밀번호가 있으면 환경변수로 넘기는 방식을 추천합니다.

PowerShell 예시:

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_password"
cd backend
.\gradlew.bat bootRun
```

## CSV 위치

초기 데이터 CSV는 여기에 둡니다.

```text
backend/src/main/resources/data/product_embedding_practice_50.csv
```

필수 컬럼:

```text
id
name
category
description
price_krw
tags
example_query
embedding_text
```

## Run Backend

```powershell
cd backend
.\gradlew.bat bootRun
```

기본 주소:

```text
http://localhost:8080
```

## API

### CSV Import

CSV 파일을 MySQL `products` 테이블에 저장합니다.

```http
POST /api/admin/products/import-csv
```

Response:

```json
{
  "importedCount": 50,
  "message": "CSV import completed"
}
```

### Product List

```http
GET /api/products
```

### Product Detail

```http
GET /api/products/{id}
```

### Keyword Search

검색 대상은 `name`, `category`, `description`, `tags`입니다.

```http
GET /api/search?query=마우스
```

### Admin Product Create

```http
POST /api/admin/products
Content-Type: application/json
```

```json
{
  "name": "로지텍 M331 무소음 무선 마우스",
  "category": "마우스",
  "description": "조용한 클릭음과 안정적인 무선 연결을 제공하는 사무용 마우스",
  "priceKrw": 25000,
  "tags": "사무용, 조용한 클릭, 무선, 가성비",
  "exampleQuery": "조용한 사무용 마우스 추천",
  "embeddingText": "로지텍 M331 무소음 무선 마우스. 사무용, 조용한 클릭, 무선, 가성비."
}
```

### Admin Product Update

```http
PUT /api/admin/products/{id}
Content-Type: application/json
```

Body 형식은 생성 API와 같습니다.

### Admin Product Delete

```http
DELETE /api/admin/products/{id}
```

## Database Tables

### products

상품 기본 정보를 저장합니다.

| Column | Description |
| --- | --- |
| id | 상품 ID |
| name | 상품명 |
| category | 카테고리 |
| description | 상품 설명 |
| price_krw | 가격 |
| tags | 검색 태그 |
| example_query | 예시 검색어 |
| embedding_text | 임베딩 생성에 사용할 상품 설명 텍스트 |

### product_embeddings

다음 단계에서 상품 임베딩 벡터를 저장할 테이블입니다.

| Column | Description |
| --- | --- |
| id | 임베딩 ID |
| product_id | 상품 ID |
| model | 임베딩 모델명 |
| embedding_json | 임베딩 벡터 JSON 문자열 |
| created_at | 생성 시각 |

## Next Step

다음 단계에서는 OpenAI Embeddings API를 붙여서 다음 기능을 구현하면 됩니다.

- 상품 생성/수정 시 해당 상품의 임베딩 생성
- `product_embeddings.embedding_json`에 벡터 저장
- `POST /api/semantic-search` 추가
- cosine similarity 기반 검색 결과 반환

## Local Python Embedding Batch

이 프로젝트는 외부 유료 API(OpenAI 등)를 사용하지 않고, 로컬 Python 배치 스크립트에서 `sentence-transformers` 모델로 상품 임베딩을 생성합니다.

Spring 서버는 Python을 실행하지 않습니다. 사람이 터미널에서 배치를 직접 실행하면 MySQL의 `products` 테이블을 읽고, `product_embeddings` 테이블에 아직 없는 상품만 임베딩을 저장합니다. 이후 Spring 검색 API는 저장된 `product_embeddings.embedding_json`을 읽어 유사도 계산을 담당하는 구조입니다.

기본 모델:

```text
sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2
```

## FastAPI Query Embedding Server

검색어 임베딩은 Spring에서 Python 파일을 직접 실행하지 않고 별도 FastAPI 서버를 호출해서 생성합니다. 이 서버는 상품 임베딩 배치와 같은 모델을 사용합니다.

```text
sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2
```

서버 시작 시 모델을 한 번만 로딩하고, `POST /embed`로 검색어 임베딩을 반환합니다.

```powershell
cd embedding-server
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

Request:

```http
POST http://localhost:8000/embed
Content-Type: application/json
```

```json
{
  "text": "가볍고 배터리 오래가는 노트북"
}
```

Response:

```json
{
  "model": "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
  "embedding": [0.123, -0.456]
}
```

## Semantic Product Search

Spring 검색 API는 FastAPI 임베딩 서버의 `POST /embed`를 호출해 검색어 임베딩을 받고, MySQL `product_embeddings.embedding_json`에 저장된 상품 임베딩과 cosine similarity를 계산합니다.

```http
GET /api/products/search?query=가볍고 배터리 오래가는 노트북&limit=5
```

Response:

```json
[
  {
    "id": 1,
    "name": "상품명",
    "category": "노트북",
    "description": "상품 설명",
    "priceKrw": 1290000,
    "tags": "가벼움, 배터리",
    "similarity": 0.8234
  }
]
```

FastAPI 서버 주소는 `backend/src/main/resources/application.properties` 또는 `backend/.env`에서 설정할 수 있습니다.

```properties
EMBEDDING_SERVER_URL=http://localhost:8000
```

전체 실행 순서:

```text
1. MySQL 실행
2. Spring CSV import로 products 적재
3. embedding-batch/generate_embeddings.py 실행으로 product_embeddings 적재
4. embedding-server FastAPI 실행
5. Spring GET /api/products/search 호출
```

실행 방법:

```powershell
cd embedding-batch
pip install -r requirements.txt
python generate_embeddings.py
```

기본적으로 배치는 Spring 서버와 같은 `backend/.env`를 읽습니다.

지원하는 Spring 환경 변수:

```properties
MYSQL_DATABASE=product_embedding_search
MYSQL_USERNAME=product_user
MYSQL_PASSWORD=your_password
```

배치만 별도 DB 설정으로 실행하고 싶으면 `embedding-batch/.env.example`을 복사해서 `embedding-batch/.env`를 만들 수 있습니다. 이 파일은 `backend/.env`보다 우선 적용됩니다.

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

실행 후 `product_embeddings` 테이블의 `embedding_json` 컬럼에 JSON 배열 문자열 형태의 임베딩 벡터가 저장됩니다.

현재 흐름:

```text
Python 배치로 임베딩 생성 -> MySQL 저장 -> Spring 검색 API에서 유사도 계산
```

## Local Search Run Commands

Use 3 terminals to run local semantic search.

### 1. FastAPI embedding server

```powershell
cd C:\dev\product-embedding-search\embedding-server
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

### 2. Spring backend

Default port is `8080`.

```powershell
cd C:\dev\product-embedding-search\backend
$env:EMBEDDING_SERVER_URL="http://localhost:8000"
.\gradlew.bat bootRun
```

If port `8080` is already in use, run Spring on `8081`.

```powershell
cd C:\dev\product-embedding-search\backend
$env:SERVER_PORT="8081"
$env:EMBEDDING_SERVER_URL="http://localhost:8000"
.\gradlew.bat bootRun
```

### 3. Product embeddings batch

Run this once before search if `product_embeddings` is empty.

```powershell
cd C:\dev\product-embedding-search\embedding-batch
pip install -r requirements.txt
python generate_embeddings.py
```

### 4. Search API test

When using `curl.exe` in PowerShell, encode the query text first.

```powershell
$query = [uri]::EscapeDataString("<your Korean query>")
curl.exe "http://localhost:8081/api/products/search?query=$query&limit=5"
```

If Spring is running on `8080`, use this URL instead.

```powershell
$query = [uri]::EscapeDataString("<your Korean query>")
curl.exe "http://localhost:8080/api/products/search?query=$query&limit=5"
```

Frontend code should use `URLSearchParams` or axios `params` so query text is encoded automatically.
