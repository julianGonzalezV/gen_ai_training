package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ChatResponseDto {
    private String question;
    private String response;
}
