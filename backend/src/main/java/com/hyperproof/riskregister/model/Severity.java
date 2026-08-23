package com.hyperproof.riskregister.model;

/**
 * Severity bands applied to both inherent and residual scores for display.
 * Bands: Low 1-5, Medium 6-12, High 13-19, Critical 20-25.
 */
public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static Severity fromScore(int score) {
        if (score < 1 || score > 25) {
            throw new IllegalArgumentException("Score must be between 1 and 25, was " + score);
        }
        if (score <= 5) {
            return LOW;
        } else if (score <= 12) {
            return MEDIUM;
        } else if (score <= 19) {
            return HIGH;
        } else {
            return CRITICAL;
        }
    }
}
