# Product Embedding Search

Spring Boot 기반의 상품 의미 검색 API 서버입니다.

상품 정보를 MySQL에 저장하고, 상품 설명 텍스트를 임베딩하여 사용자 자연어 검색어와 가장 유사한 상품을 추천합니다.

사용자는 다음과 같은 자연어 검색어로 상품을 검색할 수 있습니다.

```text
조용한 사무용 마우스 추천
대학생이 들고 다닐 가벼운 노트북
QHD 고주사율 게이밍 모니터
저렴한 노이즈캔슬링 이어폰
```

---

## Features

* CSV 기반 초기 상품 데이터 적재
* MySQL 기반 상품 데이터 관리
* 상품 설명 텍스트 임베딩 저장
* 사용자 검색어 임베딩
* Cosine Similarity 기반 상품 추천
* REST API 제공
* React 프론트엔드 연동 예정

---

## Tech Stack

### Backend

* Java 21
* Spring Boot 3
* Gradle
* Spring Data JPA
* MySQL
* Apache Commons CSV
* OpenAI Embeddings API

### Frontend

* React
* Vite
* JavaScript
* CSS

---

## Architecture

```text
Client
  ↓
Spring Boot REST API
  ↓
MySQL
```

임베딩 검색 흐름은 다음과 같습니다.

```text
Product CSV
  ↓
MySQL products table
  ↓
Embedding generation API
  ↓
MySQL product_embeddings table
  ↓
User search query
  ↓
OpenAI Embeddings API
  ↓
Cosine similarity
  ↓
Recommended products
```

---

## Project Structure

```text
product-embedding-search/
├── README.md
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/productembedding/
│   │   │       ├── ProductEmbeddingApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       ├── config/
│   │   │       └── exception/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data/
│   │           └── product_embedding_practice_50.csv
│   └── test/
│       └── java/
```

---

## Database

### products

상품 기본 정보를 저장합니다.

| Column         | Description        |
| -------------- | ------------------ |
| id             | 상품 ID              |
| name           | 상품명                |
| category       | 카테고리               |
| description    | 상품 설명              |
| price_krw      | 가격                 |
| tags           | 상품 태그              |
| example_query  | 예시 검색어             |
| embedding_text | 임베딩에 사용할 상품 설명 텍스트 |

### product_embeddings

상품 임베딩 벡터를 저장합니다.

| Column         | Description |
| -------------- | ----------- |
| id             | 임베딩 ID      |
| product_id     | 상품 ID       |
| model          | 임베딩 모델명     |
| embedding_json | 임베딩 벡터 JSON |
| created_at     | 생성 시각       |

`embedding_json`에는 임베딩 벡터를 JSON 배열 문자열로 저장합니다.

```json
[0.0123, -0.0456, 0.0789]
```

---

## Environment Variables

OpenAI API 키는 환경변수로 관리합니다.

```bash
OPENAI_API_KEY=your_openai_api_key
```

---

## MySQL Setup

로컬 MySQL에서 다음 데이터베이스를 생성합니다.

```sql
CREATE DATABASE product_embedding_search
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

---

## Application Properties

`src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/product_embedding_search?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

openai.embedding.model=text-embedding-3-small
```

---

## Run

```bash
./gradlew bootRun
```

Windows 환경에서는 다음 명령어를 사용할 수 있습니다.

```bash
gradlew.bat bootRun
```

---

## API

### Get Products

전체 상품 목록을 조회합니다.

```http
GET /api/products
```

### Get Product By ID

상품 ID로 단일 상품을 조회합니다.

```http
GET /api/products/{id}
```

### Keyword Search

상품명, 카테고리, 설명, 태그를 기준으로 단순 키워드 검색을 수행합니다.

```http
GET /api/search?query=마우스
```

### Create Product Embeddings

상품의 `embedding_text`를 임베딩하여 DB에 저장합니다.

```http
POST /api/admin/embeddings/products
```

Response:

```json
{
  "createdCount": 50,
  "message": "Product embeddings created successfully"
}
```

### Semantic Search

사용자 검색어를 임베딩하고, DB에 저장된 상품 임베딩과 비교하여 유사도가 높은 상품을 반환합니다.

```http
POST /api/semantic-search
Content-Type: application/json
```

Request:

```json
{
  "query": "조용한 사무용 마우스 추천",
  "topK": 5
}
```

Response:

```json
{
  "query": "조용한 사무용 마우스 추천",
  "results": [
    {
      "id": 1,
      "name": "로지텍 M331 무소음 무선 마우스",
      "category": "마우스",
      "description": "조용한 클릭음과 안정적인 무선 연결을 제공하는 사무용 마우스",
      "priceKrw": 25000,
      "tags": "사무용, 조용한 클릭, 무선, 가성비",
      "similarity": 0.8732
    }
  ]
}
```

---

## Search Flow

```text
User query
  ↓
Create query embedding
  ↓
Load product embeddings from DB
  ↓
Calculate cosine similarity
  ↓
Sort by similarity
  ↓
Return top K products
```

---

## Notes

* 상품 데이터는 최초 실행 시 CSV에서 MySQL로 적재합니다.
* 상품 임베딩은 관리용 API를 통해 생성합니다.
* 검색 요청 시에는 사용자 검색어만 실시간으로 임베딩합니다.
* 상품 임베딩은 검색 요청마다 다시 생성하지 않습니다.
* 프론트엔드는 별도 React 프로젝트로 구현할 예정입니다.
