package com.aaryan.resumeai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private Long id;
    private String name;
    private String email;
    private String skills;
    private Integer experience;
    private String location;
    private Double matchScore;
    private String status;
    private AIInsightDTO aiInsight;
}