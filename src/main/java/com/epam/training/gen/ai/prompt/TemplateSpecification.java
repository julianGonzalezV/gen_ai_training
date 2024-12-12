package com.epam.training.gen.ai.prompt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSpecification {
    private String template;
    private String condition;
}
