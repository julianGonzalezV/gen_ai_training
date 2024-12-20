package com.epam.training.gen.ai.controller;

import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.training.gen.ai.dto.ChatRequestDto;
import com.epam.training.gen.ai.dto.ChatResponseDto;
import com.epam.training.gen.ai.dto.VectorRequestDto;
import com.epam.training.gen.ai.dto.VectorResponseDto;
import com.epam.training.gen.ai.service.SemanticKernelService;
import com.epam.training.gen.ai.vector.VectorStorageService;
import io.qdrant.client.grpc.Points;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai/api")
public class ChatController {
    private final SemanticKernelService semanticKernelService;
    private final VectorStorageService vectorStorageService;

    public ChatController(SemanticKernelService semanticKernelService, VectorStorageService vectorStorageService) {
        this.semanticKernelService = semanticKernelService;
        this.vectorStorageService = vectorStorageService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> getChatResponse(@RequestBody ChatRequestDto chatRequest,  @RequestParam(required = false) String modelName) {
        ChatResponseDto response = semanticKernelService.responseGeneration(chatRequest, modelName);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/vector/embeddings")
    public ResponseEntity<List<EmbeddingItem>> getEmbedded(@RequestBody VectorRequestDto vectorRequestDto) {
        List<EmbeddingItem> response = vectorStorageService.getEmbeddings(vectorRequestDto.getText());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/vector")
    public ResponseEntity<Void> saveVector(@RequestBody VectorRequestDto vectorRequestDto) throws ExecutionException, InterruptedException {
        vectorStorageService.processAndSaveText(vectorRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/vector/search")
    public ResponseEntity<List<VectorResponseDto>> getVector(@RequestBody VectorRequestDto vectorRequestDto) throws ExecutionException, InterruptedException {
        List<Points.ScoredPoint> response = vectorStorageService.search(vectorRequestDto.getText());
        List<VectorResponseDto> customResponse = response.stream()
                .map(point -> VectorResponseDto.builder()
                        .id(point.getId().getNum())
                        .version(point.getVersion())
                        .score(point.getScore())
                        .payloadKey(point.getPayloadMap().keySet().stream().findFirst().get())
                        .payloadValue(point.getPayloadMap().values().stream().findFirst().get().getStringValue())
                        .build())
                .collect(Collectors.toList());
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
}
