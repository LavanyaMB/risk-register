package com.hyperproof.riskregister.dto;

import com.hyperproof.riskregister.model.Category;
import com.hyperproof.riskregister.model.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RiskRequest {

    @NotBlank(message = "title must not be blank")
    private String title;

    private String description;

    @NotNull(message = "category is required")
    private Category category;

    private String owner;

    @NotNull(message = "likelihood is required")
    @Min(value = 1, message = "likelihood must be between 1 and 5")
    @Max(value = 5, message = "likelihood must be between 1 and 5")
    private Integer likelihood;

    @NotNull(message = "impact is required")
    @Min(value = 1, message = "impact must be between 1 and 5")
    @Max(value = 5, message = "impact must be between 1 and 5")
    private Integer impact;

    @NotNull(message = "status is required")
    private Status status;

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
}
