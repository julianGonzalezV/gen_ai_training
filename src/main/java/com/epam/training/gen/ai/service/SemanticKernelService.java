package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookDto;
import com.epam.training.gen.ai.dto.ChatRequestDto;
import com.epam.training.gen.ai.dto.ChatResponseDto;
import com.epam.training.gen.ai.prompt.PromptData;
import com.epam.training.gen.ai.prompt.TemplateSpecification;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

import org.yaml.snakeyaml.constructor.Constructor;

import java.util.List;

@Slf4j
@Service
public class SemanticKernelService {
    final Kernel kernel;
    final ChatCompletionService chatCompletionService;
    final String deploymentOrModelName;
    final InvocationContext invocationContext;
    // History to store the conversation
    final ChatHistory history;


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


    /**
     * This method generates a response to the user's input using Handlebars template
     *
     * @param requestDto - user's input
     * @return response to the user's input
     */
    public ChatResponseDto responseGenerationHandlebarsTemplate(ChatRequestDto requestDto) {
        Handlebars handlebars = new Handlebars();
        try {
            InputStream inputStream = new ClassPathResource("prompts/prompt-basic-template.yaml").getInputStream();
            Yaml yaml = new Yaml(new Constructor(TemplateSpecification.class, new LoaderOptions()));
            TemplateSpecification templateSpecification = yaml.loadAs(inputStream, TemplateSpecification.class);

            Template template = handlebars.compileInline(templateSpecification.getTemplate());
            PromptData promptData = new PromptData(requestDto.getName(), requestDto.getQuestion());
            String prompt = template.apply(promptData);

            return ChatResponseDto.builder()
                    .question(requestDto.getQuestion())
                    .response(responseGeneration(prompt))
                    .build();
        } catch (Exception e) {
            log.error("Error reading the template file", e);
            return ChatResponseDto.builder()
                    .question(requestDto.getQuestion())
                    .response("Error reading the template file")
                    .build();
        }
    }

    private String responseGeneration(String inputPrompt) {

        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(BookDto.class)
                                .toPromptString(new Gson()::toJson)
                                .build());

        history.addUserMessage(inputPrompt);

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
}
