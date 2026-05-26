package com.delivery.foodDelivery.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

import java.net.URI;

/**
 * Elasticsearch configuration that only activates when elasticsearch.enabled=true.
 * By default, Elasticsearch is DISABLED so the app can start without it.
 */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.delivery.foodDelivery.repository.elasticsearch")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Autowired
    private Environment env;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        String uriStr = env.getProperty("spring.elasticsearch.uris", "http://localhost:9200");

        System.out.println("DEBUG: ElasticsearchConfig - Resolving connection to: " + uriStr);

        if (!uriStr.startsWith("http")) {
            uriStr = "http://" + uriStr;
        }

        URI uri = URI.create(uriStr);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 9200 : uri.getPort();

        return ClientConfiguration.builder()
                .connectedTo(host + ":" + port)
                .build();
    }

    @Override
    @NonNull
    @Bean(name = "elasticsearchRestClient")
    public RestClient elasticsearchRestClient(@NonNull ClientConfiguration clientConfiguration) {
        String uriStr = env.getProperty("spring.elasticsearch.uris", "http://localhost:9200");

        if (!uriStr.startsWith("http")) {
            uriStr = "http://" + uriStr;
        }

        URI uri = URI.create(uriStr);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 9200 : uri.getPort();

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, uri.getScheme()));
        return builder.build();
    }
}
