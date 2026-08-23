package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.model.Mitigation;
import com.hyperproof.riskregister.model.Severity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Pure scoring logic, deliberately kept free of persistence/HTTP concerns so
 * it is trivial to unit test exhaustively.
 *
 * Formula rationale (see README for the full write-up):
 *
 * Inherent = likelihood * impact                         (range 1-25)
 *
 * Residual = round(inherent * remainingFraction), floored at 1, where
 *
 *   remainingFraction = PRODUCT over each mitigation m of (1 - (effectiveness(m) / 5) * MAX_REDUCTION_PER_CONTROL)
 *
 * Multiplying the "fraction of risk that survives" across mitigations (rather
 * than summing raw reductions) gives diminishing returns for stacking many
 * mediocre controls, mirrors how compliance frameworks reason about layered
 * controls, and structurally guarantees the fraction stays in (0, 1] so
 * residual can never exceed inherent and never reaches 0.
 */
@Service
public class ScoringService {

    /**
     * A single effectiveness=5 control can reduce risk by at most this fraction.
     * 0.8 means one maximally effective control leaves 20% of the inherent
     * score standing - "meaningfully lower" without ever implying a single
     * control makes a risk disappear entirely.
     */
    static final double MAX_REDUCTION_PER_CONTROL = 0.8;

    public int inherentScore(int likelihood, int impact) {
        return likelihood * impact;
    }

    public int residualScore(int inherentScore, List<Mitigation> mitigations) {
        if (mitigations == null || mitigations.isEmpty()) {
            return inherentScore;
        }

        double remainingFraction = 1.0;
        for (Mitigation m : mitigations) {
            double reduction = (m.getEffectiveness() / 5.0) * MAX_REDUCTION_PER_CONTROL;
            remainingFraction *= (1.0 - reduction);
        }

        long raw = Math.round(inherentScore * remainingFraction);
        return (int) Math.max(1, raw);
    }

    public Severity severityOf(int score) {
        return Severity.fromScore(score);
    }
}
