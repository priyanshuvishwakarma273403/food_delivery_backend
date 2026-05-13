package com.delivery.foodDelivery.config;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:${ELASTICSEARCH_URL:localhost:9200}}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:${ELASTICSEARCH_USERNAME:}}")
    private String username;

    @Value("${spring.elasticsearch.password:${ELASTICSEARCH_PASSWORD:}}")
    private String password;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        URI uri = URI.create(elasticsearchUri.startsWith("http") ? elasticsearchUri : "https://" + elasticsearchUri);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort();

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(host + ":" + port);

        if (uri.getScheme().equals("https") || port == 443) {
            builder.usingSsl();
        }

        if (username != null && !username.isEmpty()) {
            builder.withBasicAuth(username, password);
            // Explicitly set headers in ClientConfiguration as well
            builder.withHeaders(() -> {
                org.springframework.data.elasticsearch.support.HttpHeaders headers = new org.springframework.data.elasticsearch.support.HttpHeaders();
                headers.add("Content-Type", "application/json");
                headers.add("Accept", "application/json");
                return headers;
            });
        }

        return builder.build();
    }

    @Override
    @NonNull
    public RestClient elasticsearchRestClient(@NonNull ClientConfiguration clientConfiguration) {
        URI uri = URI.create(elasticsearchUri.startsWith("http") ? elasticsearchUri : "https://" + elasticsearchUri);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort();

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, uri.getScheme()));

        if (username != null && !username.isEmpty()) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));

            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                
                // Add interceptor for preemptive auth and compatibility fixes
                httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                    // Preemptive Basic Auth header
                    String auth = username + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                    request.setHeader("Authorization", "Basic " + encodedAuth);

                    // Force application/json and strip any 'compatible-with=8' added by the client
                    request.setHeader("Content-Type", "application/json");
                    request.setHeader("Accept", "application/json");
                });
                
                return httpClientBuilder;
            });
        }

        // Set default headers on the builder level too
        Header[] defaultHeaders = new Header[]{
            new BasicHeader("Content-Type", "application/json"),
            new BasicHeader("Accept", "application/json")
        };
        builder.setDefaultHeaders(defaultHeaders);

        return builder.build();
    }
}
