package com.hyperproof.riskregister.dto;

import com.hyperproof.riskregister.model.Category;
import com.hyperproof.riskregister.model.Severity;
import com.hyperproof.riskregister.model.Status;

import java.time.Instant;
import java.util.List;

public class RiskResponse {

    private String id;
    private String title;
    private String description;
    private Category category;
    private String owner;
    private Integer likelihood;
    private Integer impact;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

    private int inherentScore;
    private Severity inherentSeverity;
    private int residualScore;
    private Severity residualSeverity;

    private int mitigationCount;
    private List<MitigationResponse> mitigations;

    // --- getters / setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getLikelihood() {
        return likelihood;
    }

    public void setLikelihood(Integer likelihood) {
        this.likelihood = likelihood;
    }

    public Integer getImpact() {
        return impact;
    }

    public void setImpact(Integer impact) {
        this.impact = impact;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getInherentScore() {
        return inherentScore;
    }

    public void setInherentScore(int inherentScore) {
        this.inherentScore = inherentScore;
    }

    public Severity getInherentSeverity() {
        return inherentSeverity;
    }

    public void setInherentSeverity(Severity inherentSeverity) {
        this.inherentSeverity = inherentSeverity;
    }

    public int getResidualScore() {
        return residualScore;
    }

    public void setResidualScore(int residualScore) {
        this.residualScore = residualScore;
    }

    public Severity getResidualSeverity() {
        return residualSeverity;
    }

    public void setResidualSeverity(Severity residualSeverity) {
        this.residualSeverity = residualSeverity;
    }

    public int getMitigationCount() {
        return mitigationCount;
    }

    public void setMitigationCount(int mitigationCount) {
        this.mitigationCount = mitigationCount;
    }

    public List<MitigationResponse> getMitigations() {
        return mitigations;
    }

    public void setMitigations(List<MitigationResponse> mitigations) {
        this.mitigations = mitigations;
    }
}
