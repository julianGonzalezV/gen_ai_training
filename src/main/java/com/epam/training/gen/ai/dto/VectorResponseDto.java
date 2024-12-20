package com.epam.training.gen.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.qdrant.client.grpc.JsonWithInt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
