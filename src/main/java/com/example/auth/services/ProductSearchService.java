package com.example.auth.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.auth.entities.ProductDocument;
import com.example.auth.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ElasticsearchClient elasticsearchClient;

    // ─── Index a product ──────────────────────────────────
    public ProductDocument indexProduct(ProductDocument product) {
        return productRepository.save(product);
    }

    // ─── Simple search ────────────────────────────────────
    public List<ProductDocument> search(String query) {
        return productRepository
                .findByNameContainingOrDescriptionContaining(query, query);
    }

    // ─── Advanced full text search ────────────────────────
    public List<ProductDocument> advancedSearch(String query) throws IOException {
        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .query(q -> q
                                .multiMatch(m -> m
                                        .query(query)
                                        .fields("name^3", "description^1", "brand^2", "tags^2")
                                        .fuzziness("AUTO") // typo tolerance!
                                )
                        ),
                ProductDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(Hit::source)
                .toList();
    }

    // ─── Autocomplete suggestions ─────────────────────────
    public List<String> suggest(String prefix) throws IOException {
        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .query(q -> q
                                .matchPhrasePrefix(m -> m
                                        .field("name")
                                        .query(prefix)
                                )
                        )
                        .size(5), // top 5 suggestions
                ProductDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source().getName())
                .toList();
    }

    // ─── Filter by price range ────────────────────────────
    public List<ProductDocument> filterByPrice(Double min, Double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // ─── Filter by category ───────────────────────────────
    public List<ProductDocument> filterByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // ─── Delete product from index ────────────────────────
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public Map<String, Object> facetedSearch(
            String query,
            String category,
            String brand,
            Double minPrice,
            Double maxPrice) throws IOException {

        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .query(q -> q
                                .bool(b -> {
                                    // 1. full text search
                                    if (query != null && !query.isEmpty()) {
                                        b.must(m -> m
                                                .multiMatch(mm -> mm
                                                        .query(query)
                                                        .fields("name^3", "description^1", "brand^2")
                                                        .fuzziness("AUTO")
                                                )
                                        );
                                    } else {
                                        // no query — match all
                                        b.must(m -> m.matchAll(ma -> ma));
                                    }

                                    // 2. filter by category
                                    if (category != null && !category.isEmpty()) {
                                        b.filter(f -> f
                                                .term(t -> t
                                                        .field("category")
                                                        .value(category)
                                                )
                                        );
                                    }

                                    // 3. filter by brand
                                    if (brand != null && !brand.isEmpty()) {
                                        b.filter(f -> f
                                                .term(t -> t
                                                        .field("brand")
                                                        .value(brand)
                                                )
                                        );
                                    }

                                    // 4. filter by price range
                                    if (minPrice != null || maxPrice != null) {
                                        b.filter(f -> f
                                                .range(r -> r
                                                        .number(n -> {
                                                            var field = n.field("price");
                                                            if (minPrice != null) field.gte(minPrice);
                                                            if (maxPrice != null) field.lte(maxPrice);
                                                            return field;
                                                        })
                                                )
                                        );
                                    }

                                    return b;
                                })
                        )
                        // 5. category aggregation
                        .aggregations("by_category", a -> a
                                .terms(t -> t.field("category").size(20))
                        )
                        // 6. brand aggregation
                        .aggregations("by_brand", a -> a
                                .terms(t -> t.field("brand").size(20))
                        )
                        // 7. price stats aggregation
                        .aggregations("price_stats", a -> a
                                .stats(st -> st.field("price"))
                        ),
                ProductDocument.class
        );

        // ─── extract products ──────────────────────────────────
        List<ProductDocument> products = response.hits().hits()
                .stream()
                .map(Hit::source)
                .toList();

        // ─── extract category aggregation ─────────────────────
        Map<String, Long> categoryAggs = new LinkedHashMap<>();
        response.aggregations()
                .get("by_category")
                .sterms()
                .buckets()
                .array()
                .forEach(b -> categoryAggs.put(b.key().stringValue(), b.docCount()));

        // ─── extract brand aggregation ─────────────────────────
        Map<String, Long> brandAggs = new LinkedHashMap<>();
        response.aggregations()
                .get("by_brand")
                .sterms()
                .buckets()
                .array()
                .forEach(b -> brandAggs.put(b.key().stringValue(), b.docCount()));

        // ─── extract price stats ───────────────────────────────
        var priceStats = response.aggregations()
                .get("price_stats")
                .stats();

        Map<String, Object> priceInfo = new LinkedHashMap<>();
        priceInfo.put("min", priceStats.min());
        priceInfo.put("max", priceStats.max());
        priceInfo.put("avg", priceStats.avg());
        priceInfo.put("count", priceStats.count());

        // ─── build final result ────────────────────────────────
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", response.hits().total().value());
        result.put("products", products);
        result.put("filters", Map.of(
                "categories", categoryAggs,
                "brands", brandAggs,
                "priceStats", priceInfo
        ));

        return result;
    }
}
