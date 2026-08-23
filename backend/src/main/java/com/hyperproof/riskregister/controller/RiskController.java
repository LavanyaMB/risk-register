package com.hyperproof.riskregister.controller;

import com.hyperproof.riskregister.dto.RiskRequest;
import com.hyperproof.riskregister.dto.RiskResponse;
import com.hyperproof.riskregister.model.Category;
import com.hyperproof.riskregister.model.Status;
import com.hyperproof.riskregister.service.RiskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @PostMapping
    public ResponseEntity<RiskResponse> create(@Valid @RequestBody RiskRequest request) {
        RiskResponse created = riskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<RiskResponse> list(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status,
            @RequestParam(name = "sort", required = false) String sort) {
        boolean sortByResidualDesc = "residualScore,desc".equalsIgnoreCase(sort)
                || "residual".equalsIgnoreCase(sort);
        return riskService.list(category, status, sortByResidualDesc);
    }

    @GetMapping("/{id}")
    public RiskResponse getById(@PathVariable String id) {
        return riskService.getById(id);
    }

    @PutMapping("/{id}")
    public RiskResponse update(@PathVariable String id, @Valid @RequestBody RiskRequest request) {
        return riskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        riskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
