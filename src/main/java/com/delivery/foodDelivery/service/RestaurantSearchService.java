package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.elasticsearch.RestaurantDocument;
import com.delivery.foodDelivery.repository.elasticsearch.RestaurantSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Restaurant search service that gracefully degrades when Elasticsearch is not available.
 * All dependencies are optional (@Autowired required=false).
 */
@Service
@Slf4j
public class RestaurantSearchService {

    private final RestaurantSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired(required = false)
    public RestaurantSearchService(
            RestaurantSearchRepository searchRepository,
            ElasticsearchOperations elasticsearchOperations) {
        this.searchRepository = searchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // Default constructor for when Elasticsearch beans are not available
    public RestaurantSearchService() {
        this.searchRepository = null;
        this.elasticsearchOperations = null;
        log.warn("RestaurantSearchService initialized WITHOUT Elasticsearch. Search features will return empty results.");
    }

    private boolean isAvailable() {
        return searchRepository != null && elasticsearchOperations != null;
    }

    public List<RestaurantDocument> searchRestaurants(String query) {
        if (!isAvailable()) {
            log.debug("Elasticsearch is not available. Returning empty search results for query: {}", query);
            return Collections.emptyList();
        }
        try {
            String fuzzyQuery = "{ \"multi_match\": { \"query\": \"" + query + "\", \"fields\": [\"name\", \"menuItems\"], \"fuzziness\": \"AUTO\" } }";
            Query searchQuery = new StringQuery(fuzzyQuery);

            SearchHits<RestaurantDocument> searchHits = elasticsearchOperations.search(searchQuery, RestaurantDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Elasticsearch search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<RestaurantDocument> autocomplete(String prefix) {
        if (!isAvailable()) {
            log.debug("Elasticsearch is not available. Returning empty autocomplete results for prefix: {}", prefix);
            return Collections.emptyList();
        }
        try {
            Criteria criteria = new Criteria("name").startsWith(prefix);
            Query query = new CriteriaQuery(criteria);

            SearchHits<RestaurantDocument> searchHits = elasticsearchOperations.search(query, RestaurantDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Elasticsearch autocomplete failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void save(RestaurantDocument restaurantDocument) {
        if (!isAvailable()) {
            log.debug("Elasticsearch is not available. Skipping save for document: {}", restaurantDocument);
            return;
        }
        try {
            searchRepository.save(restaurantDocument);
        } catch (Exception e) {
            log.error("Failed to save document to Elasticsearch: {}", e.getMessage());
        }
    }
}
