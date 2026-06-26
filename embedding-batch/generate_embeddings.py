import json
import os
from decimal import Decimal
from pathlib import Path
from urllib.parse import urlparse

import mysql.connector
from dotenv import load_dotenv
from sentence_transformers import SentenceTransformer


MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent


def build_embedding_text(product):
    parts = [
        f"상품명: {product.get('name') or ''}",
        f"카테고리: {product.get('category') or ''}",
        f"설명: {product.get('description') or ''}",
        f"가격: {format_price(product.get('price_krw'))}",
        f"태그: {product.get('tags') or ''}",
        f"예시 검색어: {product.get('example_query') or ''}",
        f"임베딩 텍스트: {product.get('embedding_text') or ''}",
    ]
    return "\n".join(part for part in parts if part.strip())


def format_price(value):
    if value is None:
        return ""
    if isinstance(value, Decimal):
        value = int(value)
    return f"{value}원"


def load_environment():
    load_dotenv(PROJECT_ROOT / "backend" / ".env")
    load_dotenv(SCRIPT_DIR / ".env", override=True)


def get_env(name, fallback_name=None, default=None):
    value = os.getenv(name)
    if value is None or value == "":
        value = os.getenv(fallback_name) if fallback_name else None
    if value is None or value == "":
        return default
    return value


def get_required_env(name, fallback_name=None):
    value = get_env(name, fallback_name)
    if value is None or value == "":
        if fallback_name:
            raise RuntimeError(f"Missing required environment variable: {name} or {fallback_name}")
        raise RuntimeError(f"Missing required environment variable: {name}")
    return value


def parse_mysql_url(mysql_url):
    if not mysql_url:
        return {}
    jdbc_url = mysql_url.removeprefix("jdbc:")
    parsed_url = urlparse(jdbc_url)
    return {
        "host": parsed_url.hostname,
        "port": parsed_url.port,
        "database": parsed_url.path.lstrip("/") or None,
    }


def connect_mysql():
    mysql_url = parse_mysql_url(os.getenv("MYSQL_URL"))
    return mysql.connector.connect(
        host=get_env("DB_HOST", default=mysql_url.get("host") or "localhost"),
        port=int(get_env("DB_PORT", default=mysql_url.get("port") or "3306")),
        database=get_env("DB_NAME", "MYSQL_DATABASE", mysql_url.get("database")),
        user=get_required_env("DB_USER", "MYSQL_USERNAME"),
        password=get_required_env("DB_PASSWORD", "MYSQL_PASSWORD"),
        charset="utf8mb4",
        use_unicode=True,
    )


def fetch_products(cursor):
    cursor.execute(
        """
        SELECT
            id,
            name,
            category,
            description,
            price_krw,
            tags,
            example_query,
            embedding_text
        FROM products
        ORDER BY id
        """
    )
    return cursor.fetchall()


def fetch_existing_product_ids(cursor):
    cursor.execute("SELECT product_id FROM product_embeddings")
    return {row["product_id"] for row in cursor.fetchall()}


def insert_embedding(cursor, product_id, embedding_json):
    cursor.execute(
        """
        INSERT IGNORE INTO product_embeddings (product_id, model, embedding_json)
        VALUES (%s, %s, %s)
        """,
        (product_id, MODEL_NAME, embedding_json),
    )
    return cursor.rowcount


def main():
    load_environment()

    connection = connect_mysql()
    created_count = 0
    duplicate_count = 0

    try:
        cursor = connection.cursor(dictionary=True)
        products = fetch_products(cursor)
        existing_product_ids = fetch_existing_product_ids(cursor)

        products_to_embed = [
            product for product in products
            if product["id"] not in existing_product_ids
        ]
        skipped_existing_count = len(products) - len(products_to_embed)

        model = SentenceTransformer(MODEL_NAME) if products_to_embed else None

        for product in products_to_embed:
            embedding_text = build_embedding_text(product)
            embedding = model.encode(embedding_text, normalize_embeddings=True)
            embedding_json = json.dumps(embedding.tolist(), ensure_ascii=False)

            inserted_rows = insert_embedding(cursor, product["id"], embedding_json)
            if inserted_rows == 1:
                created_count += 1
            else:
                duplicate_count += 1

        connection.commit()

        skipped_count = skipped_existing_count + duplicate_count
        print(f"total products: {len(products)}")
        print(f"created embeddings: {created_count}")
        print(f"skipped products: {skipped_count}")
        print(f"model: {MODEL_NAME}")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
