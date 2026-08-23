package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.dto.MitigationRequest;
import com.hyperproof.riskregister.dto.RiskResponse;
import com.hyperproof.riskregister.exception.NotFoundException;
import com.hyperproof.riskregister.model.Mitigation;
import com.hyperproof.riskregister.model.Risk;
import com.hyperproof.riskregister.repository.MitigationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MitigationService {

    private final MitigationRepository mitigationRepository;
    private final RiskService riskService;

    public MitigationService(MitigationRepository mitigationRepository, RiskService riskService) {
        this.mitigationRepository = mitigationRepository;
        this.riskService = riskService;
    }

    public RiskResponse create(String riskId, MitigationRequest request) {
        Risk risk = riskService.findOrThrow(riskId);

        Mitigation mitigation = new Mitigation();
        mitigation.setRisk(risk);
        mitigation.setDescription(request.getDescription());
        mitigation.setEffectiveness(request.getEffectiveness());

        risk.getMitigations().add(mitigation);
        // Risk is managed/dirty-checked; saving cascades to the new mitigation.
        return riskService.toResponse(risk);
    }

    public RiskResponse update(String riskId, String mitigationId, MitigationRequest request) {
        Risk risk = riskService.findOrThrow(riskId);
        Mitigation mitigation = findWithinRisk(risk, mitigationId);

        mitigation.setDescription(request.getDescription());
        mitigation.setEffectiveness(request.getEffectiveness());

        return riskService.toResponse(risk);
    }

    public RiskResponse delete(String riskId, String mitigationId) {
        Risk risk = riskService.findOrThrow(riskId);
        Mitigation mitigation = findWithinRisk(risk, mitigationId);

        risk.getMitigations().remove(mitigation);
        mitigationRepository.delete(mitigation);

        return riskService.toResponse(risk);
    }

    private Mitigation findWithinRisk(Risk risk, String mitigationId) {
        return risk.getMitigations().stream()
                .filter(m -> m.getId().equals(mitigationId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Mitigation " + mitigationId + " not found on risk " + risk.getId()));
    }
}
