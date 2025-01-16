package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.ChatRequestDto;
import com.epam.training.gen.ai.dto.ChatResponseDto;
import com.epam.training.gen.ai.dto.VectorRequestDto;
import com.epam.training.gen.ai.vector.VectorStorageService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class RAGSampleService {
    public static final String QUESTION = "\n\nQuestion: ";

    private final VectorStorageService vectorStorageService;
    private final ChatHistory history;
    private final InvocationContext invocationContext;
    private final Map<String, ChatCompletionService> chatCompletionServices;
    private final String defaultModelName;

    public RAGSampleService(VectorStorageService vectorStorageService, ChatHistory history, InvocationContext invocationContext, Map<String, ChatCompletionService> chatCompletionServices, @Value("${client-openai-default-model-name}") String defaultModelName) {
        this.vectorStorageService = vectorStorageService;
        this.history = history;
        this.invocationContext = invocationContext;
        this.chatCompletionServices = chatCompletionServices;
        this.defaultModelName = defaultModelName;
    }

    public void uploadPdfToKnowledgeSource(MultipartFile file) throws IOException, ExecutionException, InterruptedException {
        String text = extractTextFromPdf(file);
        List<String> chunks = splitTextIntoChunks(text, 500); // Split text into chunks of 500 characters
        for (String chunk : chunks) {
            vectorStorageService.processAndSaveText(VectorRequestDto.builder().text(chunk).topic("").build());
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }

    public ChatResponseDto getChatResponse(ChatRequestDto requestDto, String modelName) {
        try {
            String userPrompt = requestDto.getQuestion();

            List<String> embeddingResults = vectorStorageService.searchEmbeddings(userPrompt);
            String embeddingAsString = embeddingResults.get(0);
            log.info("EmbeddingResults: " + embeddingAsString);

            String model = StringUtils.isNoneEmpty(modelName) ? modelName : defaultModelName;
            ChatCompletionService chatCompletionService = chatCompletionServices.get(model);
            if (chatCompletionService == null) {
                throw new IllegalArgumentException("Model not found: " + modelName);
            }

            String lLmQuery = embeddingAsString + QUESTION + userPrompt;

            return ChatResponseDto.builder()
                    .question(requestDto.getQuestion())
                    .response(getResult(lLmQuery, chatCompletionService))
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
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .build();
    }
}
