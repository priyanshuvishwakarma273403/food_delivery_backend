package com.delivery.foodDelivery.config;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.net.URI;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:localhost:9200}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
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
        }

        return builder.build();
    }

    @Override
    @NonNull
    public RestClient elasticsearchRestClient(@NonNull ClientConfiguration clientConfiguration) {
        // Manually customize the RestClient to fix OpenSearch 406 errors
        // by forcing standard JSON headers without compatibility flags.
        return org.springframework.data.elasticsearch.client.RestClients.create(clientConfiguration)
                .builder()
                .setDefaultHeaders(new Header[]{
                    new BasicHeader("Content-Type", "application/json"),
                    new BasicHeader("Accept", "application/json")
                })
                .build();
    }
}
