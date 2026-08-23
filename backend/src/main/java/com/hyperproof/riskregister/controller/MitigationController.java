package com.hyperproof.riskregister.controller;

import com.hyperproof.riskregister.dto.MitigationRequest;
import com.hyperproof.riskregister.dto.RiskResponse;
import com.hyperproof.riskregister.service.MitigationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Mitigations are always created/updated/deleted in the context of their
 * parent risk, and every response returns the parent RiskResponse (not a
 * bare MitigationResponse) so the frontend always has the freshly
 * recomputed residual score without a second round trip.
 */
@RestController
@RequestMapping("/api/risks/{riskId}/mitigations")
public class MitigationController {

    private final MitigationService mitigationService;

    public MitigationController(MitigationService mitigationService) {
        this.mitigationService = mitigationService;
    }

    @PostMapping
    public ResponseEntity<RiskResponse> create(
            @PathVariable String riskId, @Valid @RequestBody MitigationRequest request) {
        RiskResponse updated = mitigationService.create(riskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    @PutMapping("/{mitigationId}")
    public RiskResponse update(
            @PathVariable String riskId,
            @PathVariable String mitigationId,
            @Valid @RequestBody MitigationRequest request) {
        return mitigationService.update(riskId, mitigationId, request);
    }

    @DeleteMapping("/{mitigationId}")
    public RiskResponse delete(@PathVariable String riskId, @PathVariable String mitigationId) {
        return mitigationService.delete(riskId, mitigationId);
    }
}
