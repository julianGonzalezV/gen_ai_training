package com.epam.training.gen.ai.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.epam.training.gen.ai.plugins.BooksPlugin;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticKernelConfiguration {
    @Value("${client-openai-key}")
    private String apiKey;

    @Value("${client-openai-endpoint}")
    private String apiEndpoint;

    @Value("${client-openai-deployment-name}")
    private String modelName;

    @Bean
    public OpenAIAsyncClient openAIAsyncClient() {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(apiEndpoint)
                .buildAsyncClient();
    }

    @Bean
    public ChatCompletionService chatCompletionService(OpenAIAsyncClient client) {
        return OpenAIChatCompletion.builder()
                .withModelId(modelName)
                .withOpenAIAsyncClient(client)
                .build();
    }

    @Bean
    public KernelPlugin bookKernelPlugin() {
        return KernelPluginFactory.createFromObject(
                new BooksPlugin(), "BooksPlugin");
    }

    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService, KernelPlugin bookKernelPlugin) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(bookKernelPlugin)
                .build();
    }

    @Bean
    public InvocationContext invocationContext() {
        return InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withTemperature(0)
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
