package com.example.productembedding.service;

import com.example.productembedding.dto.EmbeddingResponse;
import com.example.productembedding.dto.ProductSearchResponse;
import com.example.productembedding.model.ProductEmbedding;
import com.example.productembedding.repository.ProductEmbeddingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductSearchService {

    private static final String EMBEDDING_MODEL = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2";

    private final EmbeddingClient embeddingClient;
    private final ProductEmbeddingRepository productEmbeddingRepository;
    private final ObjectMapper objectMapper;

    public ProductSearchService(
            EmbeddingClient embeddingClient,
            ProductEmbeddingRepository productEmbeddingRepository,
            ObjectMapper objectMapper
    ) {
        this.embeddingClient = embeddingClient;
        this.productEmbeddingRepository = productEmbeddingRepository;
        this.objectMapper = objectMapper;
    }

    public List<ProductSearchResponse> search(String query, int limit) {
        if (query == null || query.isBlank() || limit < 1) {
            return List.of();
        }

        EmbeddingResponse queryEmbedding = embeddingClient.embed(query.trim());
        if (queryEmbedding == null || queryEmbedding.embedding() == null || queryEmbedding.embedding().isEmpty()) {
            throw new IllegalStateException("Embedding server returned an empty embedding.");
        }
        if (!EMBEDDING_MODEL.equals(queryEmbedding.model())) {
            throw new IllegalStateException("Unexpected embedding model: " + queryEmbedding.model());
        }

        double[] queryVector = toDoubleArray(queryEmbedding.embedding());

        return productEmbeddingRepository.findAll().stream()
                .filter(productEmbedding -> EMBEDDING_MODEL.equals(productEmbedding.getModel()))
                .map(productEmbedding -> toSearchResult(productEmbedding, queryVector))
                .sorted(Comparator.comparingDouble(ProductSearchResponse::similarity).reversed())
                .limit(limit)
                .toList();
    }

    private ProductSearchResponse toSearchResult(ProductEmbedding productEmbedding, double[] queryVector) {
        double[] productVector = parseEmbeddingJson(productEmbedding.getEmbeddingJson());
        double similarity = cosineSimilarity(queryVector, productVector);
        return ProductSearchResponse.from(productEmbedding.getProduct(), similarity);
    }

    private double[] parseEmbeddingJson(String embeddingJson) {
        try {
            List<Double> values = objectMapper.readValue(embeddingJson, new TypeReference<>() {
            });
            return toDoubleArray(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid product embedding JSON.", e);
        }
    }

    private double[] toDoubleArray(List<Double> values) {
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private double cosineSimilarity(double[] left, double[] right) {
        if (left.length != right.length) {
            throw new IllegalStateException("Embedding dimension mismatch.");
        }

        double dot = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;

        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }

        if (leftNorm == 0.0 || rightNorm == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
