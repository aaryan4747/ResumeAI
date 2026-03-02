package com.aaryan.resumeai.controller;

import com.aaryan.resumeai.dto.MatchRequest;
import com.aaryan.resumeai.dto.ResumeAnalysisResponse;
import com.aaryan.resumeai.payload.ApiResponse;
import com.aaryan.resumeai.service.ResumeAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
@Tag(name = "AI Recruitment", description = "APIs for intelligent resume matching")
public class AIController {

        private final ResumeAnalysisService resumeAnalysisService;

        public AIController(ResumeAnalysisService resumeAnalysisService) {
                this.resumeAnalysisService = resumeAnalysisService;
        }

        // ==============================
        // 1️⃣ Match Candidates Endpoint
        // ==============================
        @Operation(summary = "Match Top Candidates", description = "Matches resumes against job requirements and returns ranked candidates")
        @PostMapping("/match")
        public ResponseEntity<ApiResponse<List<ResumeAnalysisResponse>>> matchCandidates(
                        @RequestBody MatchRequest request) {

                List<ResumeAnalysisResponse> results = resumeAnalysisService.matchCandidates(request);

                ApiResponse<List<ResumeAnalysisResponse>> response = new ApiResponse<>(
                                true,
                                "Top candidates fetched successfully",
                                results);

                return ResponseEntity.ok(response);
        }

        // ==============================
        // 2️⃣ Get Top 5 Candidates Only
        // ==============================
        @Operation(summary = "Get Top 5 Candidates", description = "Returns only top 5 candidates based on skill match")
        @GetMapping("/top")
        public ResponseEntity<ApiResponse<List<ResumeAnalysisResponse>>> getTopCandidates(
                        @RequestParam String skills) {

                MatchRequest request = new MatchRequest();
                request.setSkills(skills);

                List<ResumeAnalysisResponse> results = resumeAnalysisService.matchCandidates(request);

                List<ResumeAnalysisResponse> topCandidates = results.stream()
                                .limit(5)
                                .toList();

                ApiResponse<List<ResumeAnalysisResponse>> response = new ApiResponse<>(
                                true,
                                "Top 5 candidates fetched successfully",
                                topCandidates);

                return ResponseEntity.ok(response);
        }

}