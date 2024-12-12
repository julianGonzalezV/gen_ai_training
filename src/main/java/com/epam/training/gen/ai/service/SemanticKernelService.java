package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.ChatRequestDto;
import com.epam.training.gen.ai.dto.ChatResponseDto;
import com.epam.training.gen.ai.plugins.EmployeeVacationCalculatorPlugin;
import com.epam.training.gen.ai.plugins.LightsPlugin;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SemanticKernelService {
    private final Map<String, ChatCompletionService> chatCompletionServices;
    private final String defaultModelName;
    private final InvocationContext invocationContext;
    // History to store the conversation
    private final ChatHistory history;


    public SemanticKernelService(Map<String, ChatCompletionService> chatCompletionServices,
                                 ChatHistory history,
                                 InvocationContext invocationContext,
                                 @Value("${client-openai-default-model-name}") String defaultModelName) {
        this.chatCompletionServices = chatCompletionServices;
        this.defaultModelName = defaultModelName;
        this.invocationContext = invocationContext;
        this.history = history;
    }


    /**
     * This method generates a response to the user's input using Handlebars template
     *
     * @param requestDto - user's input
     * @return response to the user's input
     */
    public ChatResponseDto responseGeneration(ChatRequestDto requestDto, String modelName) {
        try {
            String prompt = requestDto.getQuestion();

            String model = StringUtils.isNoneEmpty(modelName) ? modelName : defaultModelName;
            ChatCompletionService chatCompletionService = chatCompletionServices.get(model);
            if (chatCompletionService == null) {
                throw new IllegalArgumentException("Model not found: " + modelName);
            }

            return ChatResponseDto.builder()
                    .question(requestDto.getQuestion())
                    .response(getResult(prompt, chatCompletionService))
                    .build();

        } catch (Exception e) {
            log.error("Error reading the template file", e);
            return ChatResponseDto.builder()
                    .question(requestDto.getQuestion())
                    .response("Error reading the template file")
                    .build();
        }
    }

    private String getResult(String inputPrompt, ChatCompletionService chatCompletionService) {
        history.addUserMessage(inputPrompt);

        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history,
                        getKernel(chatCompletionService)
                        , invocationContext)
                .block();

        results.stream().forEach(result -> {
            if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
                log.info("Assistant > " + result);
            }
            // Add the message from the agent to the chat history
            history.addMessage(result);
        });

        return results.toString();
    }


    private Kernel getKernel(ChatCompletionService chatCompletionService) {

        KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),
                "LightsPlugin");

        KernelPlugin employeeVacationCalculatorPlugin = KernelPluginFactory.createFromObject(new EmployeeVacationCalculatorPlugin(),
                "EmployeeVacationCalculatorPlugin");

        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(lightPlugin)
                .withPlugin(employeeVacationCalculatorPlugin)
                .build();
    }
}
