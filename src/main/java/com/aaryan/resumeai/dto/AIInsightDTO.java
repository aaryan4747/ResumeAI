package com.aaryan.resumeai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightDTO {
    private Double score;
    private List<String> extractedSkills;
    private String experienceLevel; // e.g., Junior, Mid, Senior
    private String assessment; // brief summary
    private List<String> strengths;
    private List<String> areasForImprovement;
}
