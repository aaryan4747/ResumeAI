package com.aaryan.resumeai.dto;

public class ResumeDTO {

    private Long id;
    private String name;
    private String email;
    private int experience;
    private String location;
    private String skills;
    private double aiScore;

    public ResumeDTO() {
    }

    public ResumeDTO(Long id, String name, String email, int experience,
                     String location, String skills, double aiScore) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.experience = experience;
        this.location = location;
        this.skills = skills;
        this.aiScore = aiScore;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getExperience() {
        return experience;
    }

    public String getLocation() {
        return location;
    }

    public String getSkills() {
        return skills;
    }

    public double getAiScore() {
        return aiScore;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void setAiScore(double aiScore) {
        this.aiScore = aiScore;
    }
}
