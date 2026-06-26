package com.example.productembedding.repository;

import com.example.productembedding.model.ProductEmbedding;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {

    @EntityGraph(attributePaths = "product")
    List<ProductEmbedding> findAll();

    void deleteByProductId(Long productId);
}
