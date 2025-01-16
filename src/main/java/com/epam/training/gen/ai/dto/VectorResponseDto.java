package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@AllArgsConstructor
@Builder
public class VectorResponseDto {
    private long id;
    private long version;
    private float score;
    private String payloadKey;
    private String payloadValue;
}
