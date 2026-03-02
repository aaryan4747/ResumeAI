package com.aaryan.resumeai.controller;

import com.aaryan.resumeai.entity.Resume;
import com.aaryan.resumeai.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
@Tag(name = "Resume Controller", description = "APIs for Resume Upload, Fetch, and Manual Updates")
public class ResumeController {

        @Autowired
        private ResumeService resumeService;

        @Operation(summary = "Get all resumes")
        @GetMapping
        public List<Resume> getAllResumes() {
                return resumeService.getAllResumes();
        }

        @Operation(summary = "Upload Resume File", description = "PDF, DOCX, or TXT")
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Resume uploadResume(@RequestPart("file") MultipartFile file) {
                return resumeService.uploadResume(file);
        }

        @Operation(summary = "Update resume details manually (Human-in-the-loop)")
        @PutMapping("/{id}")
        public Resume updateResume(@PathVariable Long id, @RequestBody Resume resumeDetails) {
                return resumeService.updateResume(id, resumeDetails);
        }

        @Operation(summary = "Bulk delete resumes (Enterprise Management)")
        @DeleteMapping("/bulk")
        public void bulkDelete(@RequestBody List<Long> ids) {
                resumeService.bulkDelete(ids);
        }

        @Operation(summary = "Export all resumes to CSV (Enterprise Reporting)")
        @GetMapping(value = "/export", produces = "text/csv")
        public ResponseEntity<String> exportResumes() {
                String csv = resumeService.generateCsv();
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=candidates.csv")
                                .contentType(MediaType.parseMediaType("text/csv"))
                                .body(csv);
        }
}