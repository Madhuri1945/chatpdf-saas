package com.example.chatpdf.chat.service;

import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.chat.dto.LLMRequest;
import com.example.chatpdf.chat.dto.LLMResponse;
import com.example.chatpdf.exceptions.llm.LLMGatewayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAILLMService implements LLMService{

    private final RestClient restClient;


    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.chat.model}")
    private String model;

    public OpenAILLMService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String generateAnswer(String question, String context, List<LLMMessage> history) {
        List<LLMMessage> messages=new ArrayList<>();
        messages.add(new LLMMessage(
                "system",
                """
                You are a helpful assistant that answers questions
                using the provided document context.

                Answer the user's question using the context.
                If the answer cannot be determined from the context,
                say that the information is not available in the document.

                Do not invent information.
                """
        ));

        for(LLMMessage message:history){
            messages.add(message);
        }
        String userPrompt= """
                DOCUMENT CONTEXT:
                %s
                QUESTION
                %s
                """.formatted(context,question);
        messages.add(new LLMMessage(
                "user",userPrompt
        ));
        LLMRequest request=new LLMRequest(model,messages);
        LLMResponse response=restClient.post().uri("/chat/completions")
                .header("Authorization","Bearer "+apiKey)
                .body(request)
                .retrieve()
                .body(LLMResponse.class);
        if (response == null ||
                response.choices() == null ||
                response.choices().isEmpty()) {

            throw new RuntimeException("Failed to generate LLM response");
        }
        return response.choices().get(0).message().content();
    }

    @Override
    public LLMResponse generateResponse(LLMRequest request){
        if (request == null
                || request.model() == null
                || request.model().isBlank()
                || request.messages() == null
                || request.messages().isEmpty()) {
            throw new IllegalArgumentException("Invalid LLM request");
        }
         LLMResponse response;
        try{
            response=restClient.post().uri("/chat/completions")
                    .header("Authorization","Bearer "+apiKey)
                    .body(request)
                    .retrieve()
                    .body(LLMResponse.class);
        }catch(RestClientResponseException ex){
            throw new LLMGatewayException(
                    "The AI service returned an HTTP error: "
                            + ex.getStatusCode().value(),
                    ex
            );
        }catch(LLMGatewayException ex){
            throw ex;
        }catch(RuntimeException ex){
            throw new LLMGatewayException(
                    "Failed to communicate with the AI service",
                    ex
            );
        }
         if(response==null || response.choices()==null || response.choices().isEmpty() || response.choices().get(0).message()==null){
             throw new LLMGatewayException("The AI service returned an empty response");
         }
         LLMResponse.Message message=response.choices().get(0).message();
        if (message == null) {
            throw new LLMGatewayException(
                    "The AI service returned an invalid response"
            );
        }

        boolean hasText = message.content() != null
                && !message.content().isBlank();

        boolean hasToolCalls = message.toolCalls() != null
                && !message.toolCalls().isEmpty();

        if (!hasText && !hasToolCalls) {
            throw new LLMGatewayException(
                    "The AI service returned neither text nor tool calls"
            );
        }

        return response;
    }
}
