package com.example.productembedding.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_krw", nullable = false)
    private int priceKrw;

    @Column(nullable = false, length = 500)
    private String tags;

    @Column(name = "example_query", length = 500)
    private String exampleQuery;

    @Column(name = "embedding_text", nullable = false, columnDefinition = "TEXT")
    private String embeddingText;

    protected Product() {
    }

    public Product(Long id, String name, String category, String description, int priceKrw, String tags,
                   String exampleQuery, String embeddingText) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.priceKrw = priceKrw;
        this.tags = tags;
        this.exampleQuery = exampleQuery;
        this.embeddingText = embeddingText;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getPriceKrw() {
        return priceKrw;
    }

    public String getTags() {
        return tags;
    }

    public String getExampleQuery() {
        return exampleQuery;
    }

    public String getEmbeddingText() {
        return embeddingText;
    }

    public void update(String name, String category, String description, int priceKrw, String tags,
                       String exampleQuery, String embeddingText) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.priceKrw = priceKrw;
        this.tags = tags;
        this.exampleQuery = exampleQuery;
        this.embeddingText = embeddingText;
    }
}
