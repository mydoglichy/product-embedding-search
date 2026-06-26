import React, { useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const DEFAULT_LIMIT = 5;
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

function getProductName(product) {
  return product.name || product.productName || "이름 없는 상품";
}

function getImageUrl(product) {
  return product.imageUrl || product.image_url || "";
}

function getPrice(product) {
  return product.price ?? product.priceKrw ?? product.price_krw;
}

function formatPrice(price) {
  if (price === undefined || price === null || price === "") {
    return null;
  }

  const numberValue = Number(price);
  if (Number.isFinite(numberValue)) {
    return new Intl.NumberFormat("ko-KR").format(numberValue) + "원";
  }

  return String(price);
}

function formatSimilarity(similarity) {
  const value = Number(similarity);
  return Number.isFinite(value) ? value.toFixed(3) : null;
}

function ProductCard({ product }) {
  const imageUrl = getImageUrl(product);
  const price = formatPrice(getPrice(product));
  const similarity = formatSimilarity(product.similarity);

  return (
    <article className="product-card">
      {imageUrl ? (
        <img className="product-image" src={imageUrl} alt={getProductName(product)} />
      ) : null}
      <div className="product-content">
        <div className="product-heading">
          <h2>{getProductName(product)}</h2>
          {similarity ? <span className="similarity">similarity {similarity}</span> : null}
        </div>
        {product.category ? <p className="category">{product.category}</p> : null}
        {product.description ? <p className="description">{product.description}</p> : null}
        {price ? <p className="price">{price}</p> : null}
      </div>
    </article>
  );
}

function App() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSearch(event) {
    event.preventDefault();

    const trimmedQuery = query.trim();
    if (!trimmedQuery) {
      return;
    }

    setLoading(true);
    setError("");
    setHasSearched(true);

    try {
      const params = new URLSearchParams({
        query: trimmedQuery,
        limit: String(DEFAULT_LIMIT),
      });
      const response = await fetch(`${API_BASE_URL}/api/products/search?${params.toString()}`);

      if (!response.ok) {
        throw new Error(`Search failed with status ${response.status}`);
      }

      const data = await response.json();
      setResults(Array.isArray(data) ? data : []);
    } catch (searchError) {
      console.error(searchError);
      setResults([]);
      setError("검색 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="page-shell">
      <section className="search-panel" aria-labelledby="search-title">
        <div className="title-block">
          <p className="eyebrow">Product Embedding Search</p>
          <h1 id="search-title">상품 임베딩 검색</h1>
        </div>

        <form className="search-form" onSubmit={handleSearch}>
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="예: 가볍고 배터리 오래가는 노트북"
            aria-label="검색어"
          />
          <button type="submit" disabled={loading || !query.trim()}>
            {loading ? "검색 중" : "검색"}
          </button>
        </form>
      </section>

      <section className="results-section" aria-live="polite">
        {loading ? <p className="status-message">검색 중입니다.</p> : null}
        {!loading && error ? <p className="status-message error">{error}</p> : null}
        {!loading && !error && hasSearched && results.length === 0 ? (
          <p className="status-message">검색 결과가 없습니다.</p>
        ) : null}
        {!loading && !error && results.length > 0 ? (
          <div className="results-grid">
            {results.map((product, index) => (
              <ProductCard key={product.id ?? `${getProductName(product)}-${index}`} product={product} />
            ))}
          </div>
        ) : null}
      </section>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
