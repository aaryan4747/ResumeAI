package com.aaryan.resumeai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "ResumeUploadRequest")
public class ResumeUploadRequest {

    @Schema(
            description = "Resume PDF file",
            type = "string",
            format = "binary"
    )
    private MultipartFile file;

    @Schema(description = "Candidate name")
    private String name;

    @Schema(description = "Candidate email")
    private String email;

    @Schema(description = "Experience in years")
    private Integer experience;

    @Schema(description = "Location")
    private String location;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}