package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookDto;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SemanticKernelService {
    Kernel kernel;
    ChatCompletionService chatCompletionService;
    String deploymentOrModelName;
    InvocationContext invocationContext;
    // History to store the conversation
    ChatHistory history;


    public SemanticKernelService(Kernel kernel,
                                 ChatHistory history,
                                 ChatCompletionService chatCompletionService,
                                 InvocationContext invocationContext,
                                 @Value("${client-openai-deployment-name}") String deploymentOrModelName) {
        this.kernel = kernel;
        this.chatCompletionService = chatCompletionService;
        this.deploymentOrModelName = deploymentOrModelName;
        this.invocationContext = invocationContext;
        this.history = history;
    }

    public String responseGeneration(String inputPrompt) {
        String promptWithFormat = inputPrompt.concat("""
                When you are asked for books then return get_books from plugin
                If you are asked about other topics except books, you must answer "Sorry, I'm just answering about books from Latin American history"
                Finally, provide the output in JSON format, following next rules.
                        
                Rule 1. Sample response just when you are asked for books:
                {"books": [
                    {
                        "title": "Open Veins of Latin America: Five Centuries of the Pillage of a Continent",
                        "author": "Eduardo Galeano",
                        "year": 1971
                    },
                    {
                        "title": "Guns, Germs, and Steel: The Fates of Human Societies",
                        "author": "Jared Diamond",
                        "year": 2001
                    }
                ]}
                        
                Rule2. Sample response when you are asked for other topics, be aware that you should not include the books key for this case:
                    {"response": "Sorry, I'm just answering about books from Latin American history"}
                """);


        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(BookDto.class)
                                .toPromptString(new Gson()::toJson)
                                .build());

        history.addUserMessage(promptWithFormat);

        // Prompt AI for response to users input
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, invocationContext)
                .block();

        StringBuilder response = new StringBuilder();

        for (ChatMessageContent<?> result : results) {
            // Print the results
            if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
                log.info("Assistant > " + result);
            }
            // Add the message from the agent to the chat history
            history.addMessage(result);
            response.append(result);
        }

        return response.toString();
    }
}
