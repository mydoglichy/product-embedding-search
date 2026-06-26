package com.example.productembedding.service;

import com.example.productembedding.model.Product;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ProductService {

    private static final String CSV_PATH = "data/product_embedding_practice_50.csv";
    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "id",
            "name",
            "category",
            "description",
            "price_krw",
            "tags",
            "example_query",
            "embedding_text"
    );

    private final List<Product> products = new ArrayList<>();

    @PostConstruct
    void loadProducts() {
        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        if (!resource.exists()) {
            throw new IllegalStateException("CSV 파일을 찾을 수 없습니다: src/main/resources/" + CSV_PATH);
        }

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
                );
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            validateRequiredColumns(parser);
            products.clear();

            for (CSVRecord record : parser) {
                products.add(toProduct(record));
            }
        } catch (IOException e) {
            throw new IllegalStateException("CSV 파일을 읽는 중 오류가 발생했습니다: src/main/resources/" + CSV_PATH, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("CSV 데이터 형식이 올바르지 않습니다: " + e.getMessage(), e);
        }
    }

    public List<Product> findAll() {
        return List.copyOf(products);
    }

    public Product findById(long id) {
        return products.stream()
                .filter(product -> product.id() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다. id=" + id));
    }

    public List<Product> searchByKeyword(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        return products.stream()
                .filter(product -> containsKeyword(product, normalizedQuery))
                .toList();
    }

    private void validateRequiredColumns(CSVParser parser) {
        Set<String> actualColumns = parser.getHeaderMap().keySet();
        List<String> missingColumns = REQUIRED_COLUMNS.stream()
                .filter(requiredColumn -> !actualColumns.contains(requiredColumn))
                .sorted()
                .toList();

        if (!missingColumns.isEmpty()) {
            throw new IllegalStateException("CSV 필수 컬럼이 없습니다: " + String.join(", ", missingColumns));
        }
    }

    private Product toProduct(CSVRecord record) {
        try {
            return new Product(
                    Long.parseLong(record.get("id")),
                    record.get("name"),
                    record.get("category"),
                    record.get("description"),
                    Integer.parseInt(record.get("price_krw")),
                    record.get("tags"),
                    record.get("example_query"),
                    record.get("embedding_text")
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 컬럼 파싱 실패. line=" + record.getRecordNumber(), e);
        }
    }

    private boolean containsKeyword(Product product, String normalizedQuery) {
        return contains(product.name(), normalizedQuery)
                || contains(product.category(), normalizedQuery)
                || contains(product.description(), normalizedQuery)
                || contains(product.tags(), normalizedQuery);
    }

    private boolean contains(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }
}
