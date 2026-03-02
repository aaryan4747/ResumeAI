package com.aaryan.resumeai.service;

import com.aaryan.resumeai.dto.AIInsightDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.embedding.url:https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent}")
    private String embeddingUrl;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty())
            return List.of();

        String urlWithKey = embeddingUrl + "?key=" + apiKey;

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", text);
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "models/text-embedding-004");
        requestBody.put("content", content);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, requestBody, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> embeddingMap = (Map<String, Object>) response.getBody().get("embedding");
                if (embeddingMap != null) {
                    return (List<Double>) embeddingMap.get("values");
                }
            }
        } catch (Exception e) {
            log.error("Embedding generation failed: " + e.getMessage());
        }
        return List.of();
    }

    public Double calculateMatchScore(String resumeText, String jobSkills) {
        AIInsightDTO insight = analyzeResumeDetailed(resumeText, jobSkills);
        return insight.getScore();
    }

    public AIInsightDTO analyzeResumeDetailed(String resumeText, String jobSkills) {
        if (resumeText == null || resumeText.trim().isEmpty()) {
            return fallbackInsight(0.0, "Empty resume provided.");
        }

        try {
            String prompt = """
                    You are a professional AI recruitment agent. Analyze the following resume contextually.

                    Return your analysis ONLY in the following JSON format:
                    {
                      "score": number (0-100),
                      "extractedSkills": ["skill1", "skill2"],
                      "experienceLevel": "Junior" | "Mid" | "Senior" | "Expert",
                      "assessment": "string",
                      "strengths": ["point1", "point2"],
                      "areasForImprovement": ["point1"]
                    }

                    Job Requirements (if provided): %s

                    Resume Text:
                    %s
                    """.formatted(jobSkills != null ? jobSkills : "General analysis", resumeText);

            String responseText = callGemini(prompt);
            if (responseText != null) {
                // Clean the response if it contains markdown code blocks
                if (responseText.contains("```json")) {
                    responseText = responseText.substring(responseText.indexOf("```json") + 7);
                    responseText = responseText.substring(0, responseText.indexOf("```"));
                } else if (responseText.contains("```")) {
                    responseText = responseText.substring(responseText.indexOf("```") + 3);
                    responseText = responseText.substring(0, responseText.lastIndexOf("```"));
                }

                return objectMapper.readValue(responseText, AIInsightDTO.class);
            }
        } catch (Exception e) {
            log.error("Detailed Gemini analysis failed. Using fallback.", e);
        }

        return fallbackInsight(basicKeywordMatch(resumeText, jobSkills),
                "Detailed analysis failed, using fallback keyword scoring.");
    }

    private String callGemini(String prompt) {
        String urlWithKey = geminiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    if (contentMap != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: " + e.getMessage());
        }

        return null;
    }

    private AIInsightDTO fallbackInsight(Double score, String message) {
        return AIInsightDTO.builder()
                .score(score)
                .assessment(message)
                .experienceLevel("N/A")
                .extractedSkills(List.of())
                .strengths(List.of())
                .areasForImprovement(List.of())
                .build();
    }

    private Double basicKeywordMatch(String resumeText, String jobSkills) {
        if (jobSkills == null || jobSkills.trim().isEmpty() || resumeText == null || resumeText.trim().isEmpty()) {
            return 0.0;
        }

        String[] skills = jobSkills.toLowerCase().split(",");
        String resume = resumeText.toLowerCase();

        int matchCount = 0;
        for (String skill : skills) {
            if (resume.contains(skill.trim())) {
                matchCount++;
            }
        }

        return (matchCount * 100.0) / skills.length;
    }
}
