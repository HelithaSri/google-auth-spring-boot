package com.example.auth.repository;

import com.example.auth.entities.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends ElasticsearchRepository<ProductDocument, String> {

    // full text search on name and description
    List<ProductDocument> findByNameContainingOrDescriptionContaining(
            String name, String description);

    // filter by category
    List<ProductDocument> findByCategory(String category);

    // filter by brand
    List<ProductDocument> findByBrand(String brand);

    // filter by price range
    List<ProductDocument> findByPriceBetween(Double minPrice, Double maxPrice);

}
