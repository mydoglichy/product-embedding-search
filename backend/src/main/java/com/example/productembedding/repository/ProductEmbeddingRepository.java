package com.example.productembedding.repository;

import com.example.productembedding.model.ProductEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {

    void deleteByProductId(Long productId);
}
