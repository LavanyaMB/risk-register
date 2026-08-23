package com.hyperproof.riskregister.repository;

import com.hyperproof.riskregister.model.Risk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RiskRepository extends JpaRepository<Risk, String>, JpaSpecificationExecutor<Risk> {

    // Filtering is implemented via Specifications in RiskService (category and
    // status are both optional independently), and sorting-by-residual-score
    // happens in-memory in RiskService because residual score is a computed
    // value, not a persisted column - see README "Sorting by a computed field"
    // for why that trade-off is fine at this scale and what we'd do at scale.
}
