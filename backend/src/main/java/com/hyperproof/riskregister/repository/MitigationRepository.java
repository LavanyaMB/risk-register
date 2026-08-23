package com.hyperproof.riskregister.repository;

import com.hyperproof.riskregister.model.Mitigation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MitigationRepository extends JpaRepository<Mitigation, String> {
}
