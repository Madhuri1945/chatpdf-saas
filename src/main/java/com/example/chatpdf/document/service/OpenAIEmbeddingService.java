package com.example.chatpdf.document.service;

import com.example.chatpdf.document.dto.EmbeddingData;
import com.example.chatpdf.document.dto.EmbeddingRequest;
import com.example.chatpdf.document.dto.EmbeddingResponse;
import com.pgvector.PGvector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OpenAIEmbeddingService implements EmbeddingService {

    private final RestClient restClient;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.embedding.model}")
    private String model;

    public OpenAIEmbeddingService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {

        EmbeddingRequest request = new EmbeddingRequest(model, texts);

        EmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null) {
            throw new RuntimeException("Failed to generate embeddings");
        }

        return response.data()
                .stream()
                .map(EmbeddingData::embedding)
                .map(this::toFloatArray)
                .toList();
    }

    private float[] toFloatArray(List<Float> values) {
        float[] vector = new float[values.size()];

        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.get(i);
        }

        return vector;
    }
}