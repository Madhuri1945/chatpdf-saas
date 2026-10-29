
package com.example.chatpdf.agent;

import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.agent.tool.ToolDispatcher;
import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.chat.dto.LLMRequest;
import com.example.chatpdf.chat.dto.LLMResponse;
import com.example.chatpdf.chat.dto.LLMToolCall;
import com.example.chatpdf.chat.service.OpenAILLMService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentLoop {

    private static final int MAX_ITERATIONS = 5;

    private final OpenAILLMService openAILLMService;
    private final ToolDispatcher toolDispatcher;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    public AgentLoop(
            OpenAILLMService openAILLMService,
            ToolDispatcher toolDispatcher,
            ObjectMapper objectMapper
    ) {
        this.openAILLMService = openAILLMService;
        this.toolDispatcher = toolDispatcher;
        this.objectMapper = objectMapper;
    }

    public AgentResult run(
            Long documentId,
            Long userId,
            List<LLMMessage> messages,
            List<LLMTool> tools
    ) {
        // Ensure we can safely append messages.
        messages = new ArrayList<>(messages);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {

            // 1. Send the conversation to the LLM.
            LLMRequest request =
                    new LLMRequest(model, messages, tools, "auto");

            LLMResponse response =
                    openAILLMService.generateResponse(request);

            if (response == null
                    || response.choices() == null
                    || response.choices().isEmpty()) {
                throw new IllegalStateException(
                        "LLM returned no choices"
                );
            }

            LLMResponse.Choice choice = response.choices().get(0);
            String finishReason = choice.finishReason();

            // 2. The LLM has finished answering.
            if ("stop".equals(finishReason)) {
                if (choice.message() == null
                        || choice.message().content() == null) {
                    throw new IllegalStateException(
                            "LLM returned an empty final answer"
                    );
                }

                return new AgentResult(
                        choice.message().content(),
                        List.of()
                );
            }

            // 3. The LLM wants to execute one or more tools.
            if ("tool_calls".equals(finishReason)) {

                if (choice.message() == null
                        || choice.message().toolCalls() == null
                        || choice.message().toolCalls().isEmpty()) {
                    throw new IllegalStateException(
                            "LLM requested tools but returned no tool calls"
                    );
                }

                List<LLMToolCall> toolCalls =
                        choice.message().toolCalls();

                // Record the assistant's original tool request FIRST.
                messages.add(new LLMMessage(
                        "assistant",
                        choice.message().content(),
                        toolCalls,
                        null
                ));

                // Execute every requested tool and record its result.
                for (LLMToolCall toolCall : toolCalls) {

                    String toolCallId = toolCall.id();
                    String toolName = toolCall.function().name();
                    String arguments = toolCall.function().arguments();

                    Object result = toolDispatcher.dispatch(
                            toolName,
                            arguments,
                            documentId,
                            userId
                    );

                    String resultJson;

                    try {
                        resultJson =
                                objectMapper.writeValueAsString(result);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(
                                "Failed to serialize tool result", e
                        );
                    }

                    // Link the result to its original tool request.
                    messages.add(new LLMMessage(
                            "tool",
                            resultJson,
                            null,
                            toolCallId
                    ));
                }

                // Continue the loop with the updated conversation.
                continue;
            }

            // 4. Handle unexpected or incomplete responses.
            throw new IllegalStateException(
                    "Unexpected LLM finish reason: " + finishReason
            );
        }

        throw new IllegalStateException(
                "Agent exceeded maximum iterations: " + MAX_ITERATIONS
        );
    }
}
