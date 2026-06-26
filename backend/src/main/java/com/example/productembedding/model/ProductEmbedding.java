package com.example.productembedding.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_embeddings")
public class ProductEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "embedding_json", nullable = false, columnDefinition = "LONGTEXT")
    private String embeddingJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
    )
    private LocalDateTime updatedAt;

    protected ProductEmbedding() {
    }

    public ProductEmbedding(Product product, String model, String embeddingJson) {
        this.product = product;
        this.model = model;
        this.embeddingJson = embeddingJson;
        this.createdAt = LocalDateTime.now();
    }
}
