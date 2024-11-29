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

    public String responseGeneration(String inputQuestion) {
        String promptWithFormat = String.format("""
                Your are going to be asked for any question. Here how you should proceed to generate the response
                 - For the answer you should return 20 words.
                 - Per each question you should answer following the next JSON output format:
                    {
                    "question": "The question being asked,
                    "response": "The response coming from the AI model assistant"
                    }
                Next the question you should answer: %s
                """, inputQuestion);

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

        return results.toString();
    }

    /**
     * This version of the method is used for the free practice where the user is asked to provide books but
     * response context is limited to Latin American history, by using the get_books plugin.
     *
     * @param inputQuestion
     * @return
     */
    public String responseGenerationFreePractice(String inputQuestion) {
        String promptWithFormat = String.format("""
                Your are going to be asked for books about world history. You should limit your answers to books returned by the get_books plugin.
                Additionally, If you are asked about other topics except books, you must answer "Sorry, I'm just trained for answering about books from Latin American history"
                Following the next output format:
                Sample response when you are asked for other topics, be aware that you should not include the books key for this case:
                {"response": "Sorry, I'm just answering about books from Latin American history"}
                                
                When you are asked for books then return get_books from plugin following next output format .
                Sample response just when you are asked for books:
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
                                
                Question: %s
                """, inputQuestion);


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

        log.info("Chat Response: " + response);
        return response.toString();
    }


    public String responseGenerationHandlebarsTemplate(String inputQuestion) {
        // Load prompt from resource
        String handlebarsPromptYaml = EmbeddedResource.read("HandlebarsPrompt.yaml");


        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(BookDto.class)
                                .toPromptString(new Gson()::toJson)
                                .build());

        history.addUserMessage("promptWithFormat");

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

        log.info("Chat Response: " + response);
        return response.toString();
    }
}
