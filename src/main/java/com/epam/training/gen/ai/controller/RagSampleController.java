package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.ChatRequestDto;
import com.epam.training.gen.ai.dto.ChatResponseDto;
import com.epam.training.gen.ai.service.RAGSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/rag")
public class RagSampleController {
    @Autowired
    RAGSampleService ragSampleService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> getAnswersFromKnowledgeBase(@RequestBody ChatRequestDto chatRequest, @RequestParam(required = false) String modelName) {
        ChatResponseDto response = ragSampleService.getChatResponse(chatRequest, modelName);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/upload-knowledge-base")
    public ResponseEntity<String> uploadContextToKnowledgeBase(@RequestParam("file") MultipartFile file) {
        try {
            ragSampleService.uploadPdfToKnowledgeSource(file);
            return new ResponseEntity<>("PDF uploaded and processed successfully", HttpStatus.OK);
        } catch (IOException | ExecutionException | InterruptedException e) {
            return new ResponseEntity<>("Failed to process PDF file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
