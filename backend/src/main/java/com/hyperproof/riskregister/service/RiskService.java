package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.dto.MitigationResponse;
import com.hyperproof.riskregister.dto.RiskRequest;
import com.hyperproof.riskregister.dto.RiskResponse;
import com.hyperproof.riskregister.exception.BusinessRuleException;
import com.hyperproof.riskregister.exception.NotFoundException;
import com.hyperproof.riskregister.model.Category;
import com.hyperproof.riskregister.model.Mitigation;
import com.hyperproof.riskregister.model.Risk;
import com.hyperproof.riskregister.model.Status;
import com.hyperproof.riskregister.repository.RiskRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RiskService {

    private final RiskRepository riskRepository;
    private final ScoringService scoringService;

    public RiskService(RiskRepository riskRepository, ScoringService scoringService) {
        this.riskRepository = riskRepository;
        this.scoringService = scoringService;
    }

    public RiskResponse create(RiskRequest request) {
        Risk risk = new Risk();
        applyRequest(risk, request, true);
        Risk saved = riskRepository.save(risk);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RiskResponse getById(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<RiskResponse> list(Category category, Status status, boolean sortByResidualDesc) {
        Specification<Risk> spec = Specification.where(null);
        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        List<RiskResponse> results = riskRepository.findAll(spec).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        if (sortByResidualDesc) {
            results.sort(Comparator.comparingInt(RiskResponse::getResidualScore).reversed());
        }
        return results;
    }

    public RiskResponse update(String id, RiskRequest request) {
        Risk risk = findOrThrow(id);

        boolean movingToClosed = request.getStatus() == Status.CLOSED && risk.getStatus() != Status.CLOSED;
        boolean hasNoMitigations = risk.getMitigations().isEmpty();
        if (movingToClosed && hasNoMitigations) {
            throw new BusinessRuleException(
                    "Risk cannot be marked Closed with zero mitigations. Add at least one mitigation, " +
                            "or use status 'Mitigating' if controls are still being implemented. " +
                            "See README for the compliance rationale.");
        }

        applyRequest(risk, request, false);
        Risk saved = riskRepository.save(risk);
        return toResponse(saved);
    }

    public void delete(String id) {
        if (!riskRepository.existsById(id)) {
            throw new NotFoundException("Risk not found: " + id);
        }
        riskRepository.deleteById(id);
    }

    // --- helpers ---

    Risk findOrThrow(String id) {
        return riskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Risk not found: " + id));
    }

    private void applyRequest(Risk risk, RiskRequest request, boolean isCreate) {
        risk.setTitle(request.getTitle());
        risk.setDescription(request.getDescription());
        risk.setCategory(request.getCategory());
        risk.setOwner(request.getOwner());
        risk.setLikelihood(request.getLikelihood());
        risk.setImpact(request.getImpact());
        risk.setStatus(request.getStatus());
    }

    RiskResponse toResponse(Risk risk) {
        RiskResponse dto = new RiskResponse();
        dto.setId(risk.getId());
        dto.setTitle(risk.getTitle());
        dto.setDescription(risk.getDescription());
        dto.setCategory(risk.getCategory());
        dto.setOwner(risk.getOwner());
        dto.setLikelihood(risk.getLikelihood());
        dto.setImpact(risk.getImpact());
        dto.setStatus(risk.getStatus());
        dto.setCreatedAt(risk.getCreatedAt());
        dto.setUpdatedAt(risk.getUpdatedAt());

        int inherent = scoringService.inherentScore(risk.getLikelihood(), risk.getImpact());
        int residual = scoringService.residualScore(inherent, risk.getMitigations());
        dto.setInherentScore(inherent);
        dto.setInherentSeverity(scoringService.severityOf(inherent));
        dto.setResidualScore(residual);
        dto.setResidualSeverity(scoringService.severityOf(residual));

        List<MitigationResponse> mitigations = risk.getMitigations().stream()
                .map(this::toMitigationResponse)
                .collect(Collectors.toList());
        dto.setMitigations(mitigations);
        dto.setMitigationCount(mitigations.size());

        return dto;
    }

    MitigationResponse toMitigationResponse(Mitigation m) {
        MitigationResponse dto = new MitigationResponse();
        dto.setId(m.getId());
        dto.setRiskId(m.getRiskId());
        dto.setDescription(m.getDescription());
        dto.setEffectiveness(m.getEffectiveness());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
