package com.example.productembedding.dto;

import com.example.productembedding.model.Product;

public record ProductResponse(
        long id,
        String name,
        String category,
        String description,
        int priceKrw,
        String tags
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getPriceKrw(),
                product.getTags()
        );
    }
}
