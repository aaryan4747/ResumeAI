package com.aaryan.resumeai.service;

import com.aaryan.resumeai.dto.AIInsightDTO;
import com.aaryan.resumeai.dto.MatchRequest;
import com.aaryan.resumeai.dto.ResumeAnalysisResponse;
import com.aaryan.resumeai.entity.Resume;
import com.aaryan.resumeai.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class ResumeAnalysisService {

        private final ResumeRepository resumeRepository;
        private final GeminiService geminiService;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public ResumeAnalysisService(
                        ResumeRepository resumeRepository,
                        GeminiService geminiService) {

                this.resumeRepository = resumeRepository;
                this.geminiService = geminiService;
        }

        public List<ResumeAnalysisResponse> matchCandidates(
                        MatchRequest request) {

                List<Resume> resumes = resumeRepository.findAll();
                List<ResumeAnalysisResponse> results = new ArrayList<>();

                // 1. Generate embedding for query (THE ENTERPRISE SEARCH PART)
                List<Double> queryEmbedding = geminiService.generateEmbedding(request.getSkills());

                for (Resume resume : resumes) {
                        try {
                                // A. Traditional AI Match Score
                                Double score = geminiService.calculateMatchScore(
                                                resume.getResumeText(),
                                                request.getSkills());

                                // B. Semantic Similarity Score (CONCEPT MATCHING)
                                Double semanticBonus = 0.0;
                                if (!queryEmbedding.isEmpty() && resume.getEmbeddingData() != null) {
                                        try {
                                                List<Double> resumeEmbedding = objectMapper.readValue(
                                                                resume.getEmbeddingData(),
                                                                new TypeReference<List<Double>>() {
                                                                });
                                                semanticBonus = calculateCosineSimilarity(queryEmbedding,
                                                                resumeEmbedding) * 100.0;
                                        } catch (Exception ignored) {
                                        }
                                }

                                // Final Weighted Score: Use weights from request (Defaults to 70/30)
                                Double aiWeight = request.getAiWeight() != null ? request.getAiWeight() : 0.7;
                                Double semWeight = request.getSemanticWeight() != null ? request.getSemanticWeight()
                                                : 0.3;
                                Double finalScore = (score * aiWeight) + (semanticBonus * semWeight);

                                AIInsightDTO insight = null;
                                if (resume.getAnalysisData() != null) {
                                        try {
                                                insight = objectMapper.readValue(resume.getAnalysisData(),
                                                                AIInsightDTO.class);
                                        } catch (Exception ignored) {
                                        }
                                }

                                ResumeAnalysisResponse response = ResumeAnalysisResponse.builder()
                                                .id(resume.getId())
                                                .name(resume.getName())
                                                .email(resume.getEmail())
                                                .skills(resume.getSkills())
                                                .experience(resume.getExperience())
                                                .location(resume.getLocation())
                                                .matchScore(finalScore)
                                                .status(resume.getStatus().name())
                                                .aiInsight(insight)
                                                .build();

                                results.add(response);
                        } catch (Exception e) {
                                // Skip or log
                        }
                }

                results.sort((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()));
                return results;
        }

        private double calculateCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
                if (vectorA.size() != vectorB.size() || vectorA.isEmpty())
                        return 0.0;

                double dotProduct = 0.0;
                double normA = 0.0;
                double normB = 0.0;

                for (int i = 0; i < vectorA.size(); i++) {
                        dotProduct += vectorA.get(i) * vectorB.get(i);
                        normA += Math.pow(vectorA.get(i), 2);
                        normB += Math.pow(vectorB.get(i), 2);
                }

                return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        }
}
