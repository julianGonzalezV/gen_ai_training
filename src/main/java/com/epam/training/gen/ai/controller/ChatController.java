package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.ChatRequestDto;
import com.epam.training.gen.ai.dto.ChatResponseDto;
import com.epam.training.gen.ai.service.SemanticKernelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final SemanticKernelService semanticKernelService;

    public ChatController(SemanticKernelService semanticKernelService) {
        this.semanticKernelService = semanticKernelService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDto> getChatResponse(@RequestBody ChatRequestDto chatRequest,  @RequestParam(required = false) String modelName) {
        ChatResponseDto response = semanticKernelService.responseGeneration(chatRequest, modelName);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
