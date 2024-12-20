package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorRequestDto {
    private String text;
    private String topic;
}