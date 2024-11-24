package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatResponseDto {
    private String input;
    private String generatedText;
}
