package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.model.Mitigation;
import com.hyperproof.riskregister.model.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoringServiceTest {

    private final ScoringService scoring = new ScoringService();

    private Mitigation mitigationWith(int effectiveness) {
        Mitigation m = new Mitigation();
        m.setEffectiveness(effectiveness);
        m.setDescription("test mitigation");
        return m;
    }

    // ---------- inherent score ----------

    @ParameterizedTest
    @CsvSource({
            "1,1,1",
            "5,5,25",
            "3,4,12",
            "1,5,5",
            "5,1,5",
    })
    void inherentScore_isLikelihoodTimesImpact(int likelihood, int impact, int expected) {
        assertThat(scoring.inherentScore(likelihood, impact)).isEqualTo(expected);
    }

    // ---------- residual score: sanity checks called out in the brief ----------

    @Test
    void residualScore_withNoMitigations_equalsInherent() {
        int inherent = scoring.inherentScore(4, 4); // 16
        int residual = scoring.residualScore(inherent, List.of());
        assertThat(residual).isEqualTo(inherent);
    }

    @Test
    void residualScore_withNullMitigationList_equalsInherent() {
        int inherent = scoring.inherentScore(3, 3); // 9
        int residual = scoring.residualScore(inherent, null);
        assertThat(residual).isEqualTo(inherent);
    }

    @Test
    void residualScore_withOneHighlyEffectiveMitigation_isMeaningfullyLowerThanInherent() {
        int inherent = scoring.inherentScore(5, 5); // 25
        int residual = scoring.residualScore(inherent, List.of(mitigationWith(5)));

        // effectiveness 5 -> 80% of the risk is removed -> residual == 5
        assertThat(residual).isEqualTo(5);
        assertThat(residual).isLessThan(inherent);
        // "meaningfully lower" - at least a 50% reduction for a top-effectiveness control
        assertThat(residual).isLessThanOrEqualTo(inherent / 2);
    }

    @Test
    void residualScore_neverFallsBelowOne_evenWithManyStrongMitigations() {
        int inherent = scoring.inherentScore(1, 1); // 1
        List<Mitigation> mitigations = List.of(
                mitigationWith(5), mitigationWith(5), mitigationWith(5), mitigationWith(5));
        int residual = scoring.residualScore(inherent, mitigations);
        assertThat(residual).isGreaterThanOrEqualTo(1);
    }

    @Test
    void residualScore_neverFallsBelowOne_forLargeInherentWithManyMitigations() {
        int inherent = scoring.inherentScore(5, 5); // 25
        List<Mitigation> mitigations = List.of(
                mitigationWith(5), mitigationWith(5), mitigationWith(5),
                mitigationWith(5), mitigationWith(5), mitigationWith(5));
        int residual = scoring.residualScore(inherent, mitigations);
        assertThat(residual).isGreaterThanOrEqualTo(1);
    }

    @Test
    void residualScore_weakMitigation_reducesOnlySlightly() {
        int inherent = scoring.inherentScore(4, 4); // 16
        int residual = scoring.residualScore(inherent, List.of(mitigationWith(1)));
        // effectiveness 1 -> only 16% removed -> residual should stay close to inherent
        assertThat(residual).isLessThan(inherent);
        assertThat(residual).isGreaterThanOrEqualTo((int) Math.round(inherent * 0.8));
    }

    @Test
    void residualScore_multipleMitigations_hasDiminishingReturnsNotLinearStacking() {
        int inherent = scoring.inherentScore(5, 5); // 25

        int oneControl = scoring.residualScore(inherent, List.of(mitigationWith(3)));
        int twoControls = scoring.residualScore(inherent, List.of(mitigationWith(3), mitigationWith(3)));

        // adding a second identical control keeps helping...
        assertThat(twoControls).isLessThan(oneControl);
        // ...but the second control's marginal reduction is smaller than the first's
        int firstReduction = inherent - oneControl;
        int secondReduction = oneControl - twoControls;
        assertThat(secondReduction).isLessThan(firstReduction);
    }

    @Test
    void residualScore_neverExceedsInherentScore() {
        int inherent = scoring.inherentScore(2, 3); // 6
        // even a near-worthless mitigation should not push residual above inherent
        int residual = scoring.residualScore(inherent, List.of(mitigationWith(1)));
        assertThat(residual).isLessThanOrEqualTo(inherent);
    }

    // ---------- severity bands ----------

    @ParameterizedTest
    @CsvSource({
            "1,LOW",
            "5,LOW",
            "6,MEDIUM",
            "12,MEDIUM",
            "13,HIGH",
            "19,HIGH",
            "20,CRITICAL",
            "25,CRITICAL",
    })
    void severityOf_mapsScoreToCorrectBand(int score, Severity expected) {
        assertThat(scoring.severityOf(score)).isEqualTo(expected);
    }

    @Test
    void severityOf_rejectsOutOfRangeScores() {
        assertThatThrownBy(() -> scoring.severityOf(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> scoring.severityOf(26)).isInstanceOf(IllegalArgumentException.class);
    }
}
