# Product Embedding Search Practice

## 프로젝트 목표

이 프로젝트는 상품 데이터를 임베딩하여 사용자의 자연어 검색어와 가장 유사한 상품을 찾아주는 Spring Boot 기반 REST API 서버를 구현하는 연습 프로젝트이다.

사용자는 검색창에 다음과 같은 자연어 검색어를 입력할 수 있다.

```text
조용한 사무용 마우스 추천
대학생이 들고 다닐 가벼운 노트북
QHD 고주사율 게이밍 모니터
저렴한 노이즈캔슬링 이어폰
```

서버는 사용자의 검색어를 임베딩하고, 미리 임베딩해둔 상품 데이터와 cosine similarity를 계산하여 유사도가 높은 상품을 JSON으로 반환한다.

---

## 전체 흐름

```text
사용자 검색어 입력
        ↓
프론트엔드에서 Spring Boot API로 요청
        ↓
Spring Boot 서버가 검색어를 OpenAI Embeddings API로 임베딩
        ↓
서버에 저장된 상품 임베딩들과 cosine similarity 계산
        ↓
유사도 높은 상품 top K개 정렬
        ↓
JSON 응답 반환
        ↓
프론트엔드에서 검색 결과 표시
```

---

## 데이터 구조

프로젝트 루트에는 `product_embedding_practice_50.csv` 파일이 존재한다.

CSV 컬럼은 다음과 같다.

| 컬럼명            | 설명                    |
| -------------- | --------------------- |
| id             | 상품 고유 ID              |
| name           | 상품명                   |
| category       | 상품 카테고리               |
| description    | 상품 설명                 |
| price_krw      | 상품 가격                 |
| tags           | 상품 특징 태그              |
| example_query  | 예시 검색어                |
| embedding_text | 임베딩에 사용할 상품 통합 설명 텍스트 |

임베딩에는 기본적으로 `embedding_text` 컬럼을 사용한다.

---

## 구현 단계

### 1단계: CSV 기반 상품 API

먼저 임베딩 없이 CSV 파일을 읽고 상품 데이터를 API로 반환한다.

구현할 기능:

* 서버 시작 시 CSV 파일 읽기
* 상품 데이터를 메모리에 저장
* 전체 상품 목록 조회 API
* 단순 키워드 검색 API

예상 API:

```http
GET /api/products
```

```http
GET /api/search?query=마우스
```

---

### 2단계: 임베딩 기반 검색 API

OpenAI Embeddings API를 사용하여 상품 검색을 의미 기반 검색으로 확장한다.

구현할 기능:

* `embedding_text` 컬럼을 임베딩
* 상품 임베딩을 메모리에 저장
* 사용자 검색어 임베딩
* cosine similarity 계산
* 유사도 높은 상품 top K개 반환

예상 API:

```http
POST /api/semantic-search
Content-Type: application/json
```

요청 예시:

```json
{
  "query": "조용한 사무용 마우스 추천",
  "topK": 5
}
```

응답 예시:

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

### 3단계: 상품 임베딩 캐싱

상품 임베딩을 매번 새로 생성하면 속도가 느리고 API 비용이 발생하므로, 한 번 생성한 임베딩은 로컬 파일에 저장한다.

구현할 기능:

* `product_embeddings_cache.json` 파일이 있으면 캐시에서 임베딩 로드
* 캐시가 없으면 OpenAI API로 상품 임베딩 생성
* 생성된 임베딩을 캐시 파일로 저장
* 검색 시 캐시된 상품 임베딩 사용

---

## 백엔드 책임

Spring Boot 서버는 다음 역할을 담당한다.

* CSV 상품 데이터 로딩
* 상품 데이터 메모리 관리
* OpenAI Embeddings API 호출
* cosine similarity 계산
* 검색 결과 정렬
* REST API 응답 반환

프론트엔드는 별도로 구현하며, Spring Boot 서버는 JSON API만 제공한다.

---

## 기술 스택

* Java 21
* Spring Boot 3
* Maven
* Apache Commons CSV
* OpenAI Embeddings API
* REST API
* CSV 기반 메모리 저장소

---

## 환경 변수

OpenAI API 키는 환경변수로 관리한다.

```bash
OPENAI_API_KEY=your_api_key_here
```

---

## 최종 목표

최종적으로 사용자가 자연어로 원하는 상품 조건을 입력하면, 단순 키워드 일치가 아니라 의미적으로 가장 가까운 상품을 추천하는 검색 API를 구현한다.

예를 들어 사용자가 다음과 같이 입력하면,

```text
도서관에서 쓰기 좋은 조용한 마우스
```

서버는 “도서관”, “조용한”, “마우스”라는 맥락을 바탕으로 무소음 사무용 마우스 상품을 높은 순위로 반환해야 한다.
