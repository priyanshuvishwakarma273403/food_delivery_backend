package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.elasticsearch.RestaurantDocument;
import com.delivery.foodDelivery.repository.elasticsearch.RestaurantSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantSearchService {

    private final RestaurantSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public List<RestaurantDocument> searchRestaurants(String query) {
        // Fuzzy search for typo tolerance across name and menu items
        String fuzzyQuery = "{ \"multi_match\": { \"query\": \"" + query + "\", \"fields\": [\"name\", \"menuItems\"], \"fuzziness\": \"AUTO\" } }";
        Query searchQuery = new StringQuery(fuzzyQuery);
        
        SearchHits<RestaurantDocument> searchHits = elasticsearchOperations.search(searchQuery, RestaurantDocument.class);
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public List<RestaurantDocument> autocomplete(String prefix) {
        Criteria criteria = new Criteria("name").startsWith(prefix);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<RestaurantDocument> searchHits = elasticsearchOperations.search(query, RestaurantDocument.class);
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public void save(RestaurantDocument restaurantDocument) {
        searchRepository.save(restaurantDocument);
    }
}
