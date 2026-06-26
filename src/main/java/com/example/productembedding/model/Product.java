package com.example.productembedding.model;

public record Product(
        long id,
        String name,
        String category,
        String description,
        int priceKrw,
        String tags,
        String exampleQuery,
        String embeddingText
) {
}
