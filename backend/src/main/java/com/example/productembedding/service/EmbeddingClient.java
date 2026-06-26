package com.example.productembedding.service;

import com.example.productembedding.dto.EmbeddingRequest;
import com.example.productembedding.dto.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmbeddingClient {

    private final RestClient restClient;

    public EmbeddingClient(@Value("${embedding.server.url}") String embeddingServerUrl, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(embeddingServerUrl)
                .build();
    }

    public EmbeddingResponse embed(String text) {
        return restClient.post()
                .uri("/embed")
                .body(new EmbeddingRequest(text))
                .retrieve()
                .body(EmbeddingResponse.class);
    }
}
