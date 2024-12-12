package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LightDto {
    private int id;
    private String name;
    private boolean isOn;

    @Override
    public String toString() {
        return "LightModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isOn=" + isOn +
                '}';
    }
}