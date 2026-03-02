package com.aaryan.resumeai.dto;

public class MatchRequest {

    private String skills;
    private int minExperience;
    private String location;
    private Double aiWeight = 0.7;
    private Double semanticWeight = 0.3;

    public MatchRequest() {
    }

    public MatchRequest(String skills, int minExperience, String location) {
        this.skills = skills;
        this.minExperience = minExperience;
        this.location = location;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(int minExperience) {
        this.minExperience = minExperience;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getAiWeight() {
        return aiWeight;
    }

    public void setAiWeight(Double aiWeight) {
        this.aiWeight = aiWeight;
    }

    public Double getSemanticWeight() {
        return semanticWeight;
    }

    public void setSemanticWeight(Double semanticWeight) {
        this.semanticWeight = semanticWeight;
    }
}