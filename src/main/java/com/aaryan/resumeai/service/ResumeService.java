package com.aaryan.resumeai.service;

import com.aaryan.resumeai.entity.Resume;
import com.aaryan.resumeai.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AnalysisAsyncService analysisAsyncService;

    // ✅ Get all resumes
    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }

    // ✅ Upload Resume (Supports PDF, DOCX, TXT)
    public Resume uploadResume(MultipartFile file) {
        try {
            // 1️⃣ Extract text (Universal Parser)
            String resumeText = documentService.extractText(file);
            if (resumeText == null || resumeText.isEmpty()) {
                throw new RuntimeException("Unable to read resume file content.");
            }

            // 2️⃣ Initial Local Extraction
            String name = extractName(resumeText);
            String email = extractEmail(resumeText);
            Integer experience = extractExperience(resumeText);
            String skills = extractSkills(resumeText);

            // 3️⃣ Create entity
            Resume resume = Resume.builder()
                    .fileName(file.getOriginalFilename())
                    .resumeText(resumeText)
                    .name(name)
                    .email(email)
                    .experience(experience)
                    .location("Unknown")
                    .skills(skills)
                    .aiScore(0.0)
                    .status(Resume.AnalysisStatus.PENDING)
                    .build();

            Resume savedResume = resumeRepository.save(resume);

            // 4️⃣ Trigger Async AI Analysis & Embeddings (Through dedicated service)
            analysisAsyncService.processResumeAsync(savedResume.getId(), resumeText);

            return savedResume;

        } catch (Exception e) {
            throw new RuntimeException("Resume processing failed: " + e.getMessage());
        }
    }

    // ✅ Update Resume (Human-in-the-loop)
    public Resume updateResume(Long id, Resume details) {
        return resumeRepository.findById(id).map(resume -> {
            if (details.getName() != null)
                resume.setName(details.getName());
            if (details.getEmail() != null)
                resume.setEmail(details.getEmail());
            if (details.getExperience() != null)
                resume.setExperience(details.getExperience());
            if (details.getSkills() != null)
                resume.setSkills(details.getSkills());
            if (details.getLocation() != null)
                resume.setLocation(details.getLocation());
            if (details.getAiScore() != null)
                resume.setAiScore(details.getAiScore());
            return resumeRepository.save(resume);
        }).orElseThrow(() -> new RuntimeException("Resume not found with id: " + id));
    }

    // ✅ Bulk Delete (Enterprise Management)
    public void bulkDelete(List<Long> ids) {
        resumeRepository.deleteAllById(ids);
    }

    // ✅ CSV Generation (Enterprise Reporting)
    public String generateCsv() {
        List<Resume> all = resumeRepository.findAll();
        StringBuilder csv = new StringBuilder("ID,Name,Email,Experience,AI Score,Status,Skills\n");
        for (Resume r : all) {
            csv.append(r.getId() != null ? r.getId() : "").append(",")
                    .append("\"").append(r.getName() != null ? r.getName().replace("\"", "'") : "Unknown").append("\",")
                    .append("\"").append(r.getEmail() != null ? r.getEmail().replace("\"", "'") : "N/A").append("\",")
                    .append(r.getExperience() != null ? r.getExperience() : 0).append(",")
                    .append(r.getAiScore() != null ? r.getAiScore() : 0.0).append(",")
                    .append(r.getStatus()).append(",")
                    .append("\"").append(r.getSkills() != null ? r.getSkills().replace("\"", "'") : "").append("\"\n");
        }
        return csv.toString();
    }

    // =========================
    // HELPER METHODS
    // =========================

    private String extractName(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 3 &&
                    !line.toLowerCase().contains("contact") &&
                    !line.toLowerCase().contains("email") &&
                    !line.toLowerCase().contains("phone") &&
                    !line.contains("@") &&
                    !line.matches(".*\\d.*")) {
                return line;
            }
        }
        return "Unknown";
    }

    private String extractEmail(String text) {
        String regex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Not found";
    }

    private Integer extractExperience(String text) {
        String regex = "(\\d+)\\s*(year|years)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private String extractSkills(String text) {
        text = text.toLowerCase();
        StringBuilder skills = new StringBuilder();
        if (text.contains("java"))
            skills.append("Java, ");
        if (text.contains("python"))
            skills.append("Python, ");
        if (text.contains("spring"))
            skills.append("Spring Boot, ");
        if (text.contains("mysql"))
            skills.append("MySQL, ");
        if (text.contains("docker"))
            skills.append("Docker, ");
        if (text.contains("html"))
            skills.append("HTML, ");
        if (text.contains("css"))
            skills.append("CSS, ");
        if (text.contains("javascript"))
            skills.append("JavaScript, ");
        if (text.contains("machine learning"))
            skills.append("Machine Learning, ");
        if (text.contains("ai"))
            skills.append("AI, ");
        return skills.toString();
    }
}