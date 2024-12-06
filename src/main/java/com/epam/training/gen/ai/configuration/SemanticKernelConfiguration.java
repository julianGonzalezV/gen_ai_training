package com.epam.training.gen.ai.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SemanticKernelConfiguration {
    @Value("${client-openai-key}")
    private String apiKey;

    @Value("${client-openai-endpoint}")
    private String apiEndpoint;

    @Value("${client-openai-deployment-names}")
    private String[] modelNames;


    @Bean
    public OpenAIAsyncClient openAIAsyncClient() {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(apiEndpoint)
                .buildAsyncClient();
    }

    @Bean
    public Map<String, ChatCompletionService> chatCompletionService(OpenAIAsyncClient client) {
        Map<String, ChatCompletionService> services = new HashMap<>();
        Arrays.stream(modelNames).distinct()
                .forEach(modelName -> services.put(modelName, OpenAIChatCompletion.builder()
                        .withModelId(modelName)
                        .withOpenAIAsyncClient(client)
                        .build()));

        return services;
    }

    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                //.withPlugin(bookKernelPlugin) used it ti  narrow down the search/context  just for free practice
                .build();
    }

    @Bean
    public InvocationContext invocationContext() {
        return InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withTemperature(1)
                        .build())
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }

    @Bean
    public ChatHistory chatHistory() {
        return new ChatHistory();
    }
}
