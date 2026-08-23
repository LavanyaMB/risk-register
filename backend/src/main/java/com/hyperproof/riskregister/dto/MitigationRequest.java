package com.hyperproof.riskregister.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MitigationRequest {

    @NotBlank(message = "description must not be blank")
    private String description;

    @NotNull(message = "effectiveness is required")
    @Min(value = 1, message = "effectiveness must be between 1 and 5")
    @Max(value = 5, message = "effectiveness must be between 1 and 5")
    private Integer effectiveness;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEffectiveness() {
        return effectiveness;
    }

    public void setEffectiveness(Integer effectiveness) {
        this.effectiveness = effectiveness;
    }
}
