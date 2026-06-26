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
                product.id(),
                product.name(),
                product.category(),
                product.description(),
                product.priceKrw(),
                product.tags()
        );
    }
}
