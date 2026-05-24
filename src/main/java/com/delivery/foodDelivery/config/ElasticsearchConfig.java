package com.delivery.foodDelivery.config;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Autowired
    private Environment env;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        String uriStr = getProp("spring.elasticsearch.uris", "ELASTICSEARCH_URL", "localhost:9200");
        String username = getProp("spring.elasticsearch.username", "ELASTICSEARCH_USERNAME", "");
        String password = getProp("spring.elasticsearch.password", "ELASTICSEARCH_PASSWORD", "");

        System.out.println("DEBUG: ElasticsearchConfig - Resolving connection to: " + uriStr);

        // Ensure scheme exists
        if (!uriStr.startsWith("http")) {
            uriStr = (uriStr.contains("localhost") || uriStr.contains("127.0.0.1")) ? "http://" + uriStr : "https://" + uriStr;
        }

        URI uri = URI.create(uriStr);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort();

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(host + ":" + port);

        if ("https".equalsIgnoreCase(uri.getScheme()) || port == 443) {
            builder.usingSsl();
        }

        if (username != null && !username.isEmpty()) {
            builder.withBasicAuth(username, password);
            builder.withHeaders(() -> {
                org.springframework.data.elasticsearch.support.HttpHeaders headers = 
                    new org.springframework.data.elasticsearch.support.HttpHeaders();
                headers.add("Accept", "application/json");
                headers.add("Content-Type", "application/json");
                return headers;
            });
        }

        return builder.build();
    }

    @Override
    @NonNull
    @Bean(name = "elasticsearchRestClient")
    public RestClient elasticsearchRestClient(@NonNull ClientConfiguration clientConfiguration) {
        String uriStr = getProp("spring.elasticsearch.uris", "ELASTICSEARCH_URL", "localhost:9200");
        String username = getProp("spring.elasticsearch.username", "ELASTICSEARCH_USERNAME", "");
        String password = getProp("spring.elasticsearch.password", "ELASTICSEARCH_PASSWORD", "");

        if (!uriStr.startsWith("http")) {
            uriStr = (uriStr.contains("localhost") || uriStr.contains("127.0.0.1")) ? "http://" + uriStr : "https://" + uriStr;
        }

        URI uri = URI.create(uriStr);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort();

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, uri.getScheme()));

        if (username != null && !username.isEmpty()) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));

            final String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(
                    (username + ":" + password).getBytes(StandardCharsets.UTF_8));

            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                
                httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                    // Aggressively set headers to bypass any library-level overrides
                    request.setHeader("Authorization", authHeaderValue);
                    request.setHeader("Accept", "application/json");
                    request.setHeader("Content-Type", "application/json");
                    
                    // OpenSearch compatibility: remove the elastic-client-meta header if it exists
                    request.removeHeaders("X-Elastic-Client-Meta");
                });
                
                return httpClientBuilder;
            });
        }

        return builder.build();
    }

    private String getProp(String springProp, String envVar, String defaultVal) {
        String val = env.getProperty(springProp);
        if (val == null || val.isEmpty()) {
            val = env.getProperty(envVar);
        }
        return (val == null || val.isEmpty()) ? defaultVal : val;
    }
}
