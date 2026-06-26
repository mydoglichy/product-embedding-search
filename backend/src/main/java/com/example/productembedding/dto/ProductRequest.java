package com.example.productembedding.dto;

public record ProductRequest(
        String name,
        String category,
        String description,
        int priceKrw,
        String tags,
        String exampleQuery,
        String embeddingText
) {
}
