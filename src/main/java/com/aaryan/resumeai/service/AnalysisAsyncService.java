package com.aaryan.resumeai.service;

import com.aaryan.resumeai.dto.AIInsightDTO;
import com.aaryan.resumeai.entity.Resume;
import com.aaryan.resumeai.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class AnalysisAsyncService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private GeminiService geminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @Transactional
    public void processResumeAsync(Long resumeId, String resumeText) {
        log.info("Starting background analysis for resume ID: {}", resumeId);
        try {
            updateStatus(resumeId, Resume.AnalysisStatus.PROCESSING);
            log.info("Status updated to PROCESSING for ID: {}", resumeId);

            if (geminiService != null) {
                log.info("Calling Gemini for detailed analysis (ID: {})", resumeId);
                AIInsightDTO insight = geminiService.analyzeResumeDetailed(resumeText, null);
                log.info("Gemini analysis complete. Score: {} (ID: {})", insight.getScore(), resumeId);

                log.info("Generating embedding for ID: {}", resumeId);
                List<Double> embedding = geminiService.generateEmbedding(resumeText);
                log.info("Embedding generated (Size: {}) for ID: {}", embedding.size(), resumeId);

                resumeRepository.findById(resumeId).ifPresent(resume -> {
                    log.info("Saving final results to database for ID: {}", resumeId);
                    resume.setAiScore(insight.getScore());
                    resume.setStatus(Resume.AnalysisStatus.COMPLETED);

                    try {
                        resume.setAnalysisData(objectMapper.writeValueAsString(insight));
                        resume.setEmbeddingData(objectMapper.writeValueAsString(embedding));
                    } catch (Exception e) {
                        log.error("JSON serialization failed for ID: {}", resumeId, e);
                    }

                    if (insight.getExtractedSkills() != null && !insight.getExtractedSkills().isEmpty()) {
                        resume.setSkills(String.join(", ", insight.getExtractedSkills()));
                    }

                    Resume saved = resumeRepository.save(resume);
                    log.info("Background analysis successfully FINISHED for ID: {}. New status: {}", resumeId,
                            saved.getStatus());
                });
            } else {
                log.error("GeminiService is NULL! Background analysis aborted for ID: {}", resumeId);
            }
        } catch (Exception e) {
            log.error("CRITICAL ERROR in background analysis for ID: {}", resumeId, e);
            updateStatus(resumeId, Resume.AnalysisStatus.FAILED);
        }
    }

    private void updateStatus(Long id, Resume.AnalysisStatus status) {
        resumeRepository.findById(id).ifPresent(resume -> {
            resume.setStatus(status);
            resumeRepository.save(resume);
        });
    }
}
