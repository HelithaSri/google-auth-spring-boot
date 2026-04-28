package com.example.auth.controllers;


import com.example.auth.common.response.ApiResponse;
import com.example.auth.common.response.BaseController;
import com.example.auth.entities.ProductDocument;
import com.example.auth.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductSearchController extends BaseController {

    private final ProductSearchService productSearchService;

    // index a product
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDocument>> index(
            @RequestBody ProductDocument product) {
        return created(productSearchService.indexProduct(product), "Product indexed");
    }

    // simple search
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> search(
            @RequestParam String q) {
        return ok(productSearchService.search(q), "Search results");
    }

    // advanced search with typo tolerance
    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> advancedSearch(
            @RequestParam String q) throws IOException {
        return ok(productSearchService.advancedSearch(q), "Search results");
    }

    // autocomplete
    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<String>>> suggest(
            @RequestParam String q) throws IOException {
        return ok(productSearchService.suggest(q), "Suggestions");
    }

    // filter by price range
    @GetMapping("/filter/price")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> filterByPrice(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ok(productSearchService.filterByPrice(min, max), "Filtered results");
    }

    // filter by category
    @GetMapping("/filter/category")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> filterByCategory(
            @RequestParam String category) {
        return ok(productSearchService.filterByCategory(category), "Filtered results");
    }

    @GetMapping("/search/faceted")
    public ResponseEntity<ApiResponse<Map<String, Object>>> facetedSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) throws IOException {

        return ok(productSearchService.facetedSearch(q, category, brand, minPrice, maxPrice),
                "Search results");
    }
}
