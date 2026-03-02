package com.aaryan.resumeai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resume")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private Integer experience;

    private String location;

    @Column(length = 2000)
    private String skills;

    private Double aiScore;

    @Column(length = 10000)
    private String resumeText;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String analysisData; // JSON string from AIInsightDTO

    @Column(columnDefinition = "TEXT")
    private String embeddingData; // JSON string of List<Double>

    public enum AnalysisStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}