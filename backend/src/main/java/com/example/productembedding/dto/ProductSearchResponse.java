package com.example.productembedding.dto;

import com.example.productembedding.model.Product;

public record ProductSearchResponse(
        long id,
        String name,
        String category,
        String description,
        int priceKrw,
        String tags,
        double similarity
) {
    public static ProductSearchResponse from(Product product, double similarity) {
        return new ProductSearchResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getPriceKrw(),
                product.getTags(),
                similarity
        );
    }
}
