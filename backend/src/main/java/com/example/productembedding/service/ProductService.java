package com.example.productembedding.service;

import com.example.productembedding.dto.ProductRequest;
import com.example.productembedding.model.Product;
import com.example.productembedding.repository.ProductEmbeddingRepository;
import com.example.productembedding.repository.ProductRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional(readOnly = true)
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

    private final ProductRepository productRepository;
    private final ProductEmbeddingRepository productEmbeddingRepository;

    public ProductService(ProductRepository productRepository, ProductEmbeddingRepository productEmbeddingRepository) {
        this.productRepository = productRepository;
        this.productEmbeddingRepository = productEmbeddingRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다. id=" + id));
    }

    public List<Product> searchByKeyword(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return productRepository.searchByKeyword(query.trim());
    }

    @Transactional
    public Product create(ProductRequest request) {
        return productRepository.save(toProduct(null, request));
    }

    @Transactional
    public Product update(long id, ProductRequest request) {
        Product product = findById(id);
        product.update(
                request.name(),
                request.category(),
                request.description(),
                request.priceKrw(),
                request.tags(),
                request.exampleQuery(),
                request.embeddingText()
        );
        return product;
    }

    @Transactional
    public void delete(long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("상품을 찾을 수 없습니다. id=" + id);
        }
        productEmbeddingRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    @Transactional
    public int importCsv() {
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

            int importedCount = 0;
            for (CSVRecord record : parser) {
                productRepository.save(toProduct(record));
                importedCount++;
            }
            return importedCount;
        } catch (IOException e) {
            throw new IllegalStateException("CSV 파일을 읽는 중 오류가 발생했습니다: src/main/resources/" + CSV_PATH, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("CSV 데이터 형식이 올바르지 않습니다: " + e.getMessage(), e);
        }
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

    private Product toProduct(Long id, ProductRequest request) {
        return new Product(
                id,
                request.name(),
                request.category(),
                request.description(),
                request.priceKrw(),
                request.tags(),
                request.exampleQuery(),
                request.embeddingText()
        );
    }
}
