package com.example.productembedding.controller;

import com.example.productembedding.dto.ProductResponse;
import com.example.productembedding.dto.ProductSearchResponse;
import com.example.productembedding.service.ProductSearchService;
import com.example.productembedding.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService, ProductSearchService productSearchService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    @GetMapping("/products")
    public List<ProductResponse> getProducts() {
        return productService.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable long id) {
        return ProductResponse.from(productService.findById(id));
    }

    @GetMapping("/products/search")
    public List<ProductSearchResponse> searchProductsByEmbedding(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return productSearchService.search(query, limit);
    }

    @GetMapping("/search")
    public List<ProductResponse> searchProducts(@RequestParam String query) {
        return productService.searchByKeyword(query).stream()
                .map(ProductResponse::from)
                .toList();
    }
}
