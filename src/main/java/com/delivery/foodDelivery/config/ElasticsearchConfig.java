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

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:${ELASTICSEARCH_URL:localhost:9200}}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:${ELASTICSEARCH_USERNAME:}}")
    private String username;

    @Value("${spring.elasticsearch.password:${ELASTICSEARCH_PASSWORD:}}")
    private String password;

    @jakarta.annotation.PostConstruct
    public void debugConfig() {
        System.out.println("DEBUG: Elasticsearch URI: " + elasticsearchUri);
        System.out.println("DEBUG: Elasticsearch Username present: " + (username != null && !username.isEmpty()));
        if (username != null) {
            System.out.println("DEBUG: Username length: " + username.length());
        }
        if (password != null) {
            System.out.println("DEBUG: Password length: " + password.length());
        }
    }
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
                
                // Add interceptor to strip the compatibility header that causes 406 on OpenSearch
                httpClientBuilder.addInterceptorLast((HttpRequestInterceptor) (request, context) -> {
                    if (request.containsHeader("Content-Type")) {
                        Header contentType = request.getFirstHeader("Content-Type");
                        if (contentType.getValue().contains("compatible-with=8")) {
                            request.removeHeader(contentType);
                            request.addHeader("Content-Type", "application/json");
                        }
                    }
                    if (request.containsHeader("Accept")) {
                        Header accept = request.getFirstHeader("Accept");
                        if (accept.getValue().contains("compatible-with=8")) {
                            request.removeHeader(accept);
                            request.addHeader("Accept", "application/json");
                        }
                    }
                });
                
                return httpClientBuilder;
            });
        }

        // Default headers
        builder.setDefaultHeaders(new Header[]{
            new BasicHeader("Content-Type", "application/json"),
            new BasicHeader("Accept", "application/json")
        });

        return builder.build();
    }
}
