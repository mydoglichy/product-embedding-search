package com.example.productembedding.repository;

import com.example.productembedding.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select p
            from Product p
            where lower(p.name) like lower(concat('%', :query, '%'))
               or lower(p.category) like lower(concat('%', :query, '%'))
               or lower(p.description) like lower(concat('%', :query, '%'))
               or lower(p.tags) like lower(concat('%', :query, '%'))
            """)
    List<Product> searchByKeyword(@Param("query") String query);
}
