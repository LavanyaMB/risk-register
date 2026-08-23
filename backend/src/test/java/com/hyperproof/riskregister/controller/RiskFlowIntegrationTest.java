package com.hyperproof.riskregister.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RiskFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlow_createRisk_addMitigation_fetch_verifyResidualScore() throws Exception {
        // 1. Create a risk: likelihood 5, impact 5 -> inherent score 25 (Critical)
        Map<String, Object> riskPayload = Map.of(
                "title", "Unpatched production database",
                "description", "DB server is missing critical security patches",
                "category", "SECURITY",
                "owner", "Infra Team",
                "likelihood", 5,
                "impact", 5,
                "status", "OPEN"
        );

        String createRiskResponse = mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(riskPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inherentScore", is(25)))
                .andExpect(jsonPath("$.inherentSeverity", is("CRITICAL")))
                .andExpect(jsonPath("$.residualScore", is(25))) // no mitigations yet
                .andExpect(jsonPath("$.mitigationCount", is(0)))
                .andReturn().getResponse().getContentAsString();

        String riskId = objectMapper.readTree(createRiskResponse).get("id").asText();

        // 2. Add a highly effective mitigation
        Map<String, Object> mitigationPayload = Map.of(
                "description", "Apply latest security patches and enable auto-patching",
                "effectiveness", 5
        );

        mockMvc.perform(post("/api/risks/{riskId}/mitigations", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mitigationPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mitigationCount", is(1)))
                .andExpect(jsonPath("$.residualScore", is(5))) // 25 * (1 - 0.8) = 5
                .andExpect(jsonPath("$.residualSeverity", is("LOW")));

        // 3. Fetch the risk and verify the residual score reflects the mitigation
        mockMvc.perform(get("/api/risks/{id}", riskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(riskId)))
                .andExpect(jsonPath("$.mitigations", hasSize(1)))
                .andExpect(jsonPath("$.residualScore", is(5)))
                .andExpect(jsonPath("$.residualScore", lessThan((Integer) 25)));
    }

    @Test
    void createRisk_withInvalidLikelihood_returns400WithClearMessage() throws Exception {
        Map<String, Object> badPayload = Map.of(
                "title", "Bad risk",
                "category", "OPERATIONAL",
                "likelihood", 7, // out of 1-5 range
                "impact", 3,
                "status", "OPEN"
        );

        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem(containsString("likelihood"))));
    }

    @Test
    void closingRiskWithNoMitigations_isRejectedWithConflict() throws Exception {
        Map<String, Object> riskPayload = Map.of(
                "title", "Vendor lock-in risk",
                "category", "STRATEGIC",
                "likelihood", 2,
                "impact", 3,
                "status", "OPEN"
        );

        String createResponse = mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(riskPayload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String riskId = objectMapper.readTree(createResponse).get("id").asText();

        Map<String, Object> closePayload = Map.of(
                "title", "Vendor lock-in risk",
                "category", "STRATEGIC",
                "likelihood", 2,
                "impact", 3,
                "status", "CLOSED"
        );

        mockMvc.perform(put("/api/risks/{id}", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closePayload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("zero mitigations")));
    }

    @Test
    void listRisks_sortedByResidualScoreDescending() throws Exception {
        createRiskWithScore("Low risk", 1, 2); // inherent 2, inserted first
        createRiskWithScore("High risk no mitigation", 5, 4); // inherent 20, inserted second

        mockMvc.perform(get("/api/risks").param("sort", "residualScore,desc"))
                .andExpect(status().isOk())
                // despite insertion order, the higher-scoring risk should sort first
                .andExpect(jsonPath("$[0].title", is("High risk no mitigation")))
                .andExpect(jsonPath("$[0].residualScore", is(20)))
                .andExpect(jsonPath("$[1].title", is("Low risk")))
                .andExpect(jsonPath("$[1].residualScore", is(2)));
    }

    private void createRiskWithScore(String title, int likelihood, int impact) throws Exception {
        Map<String, Object> payload = Map.of(
                "title", title,
                "category", "OPERATIONAL",
                "likelihood", likelihood,
                "impact", impact,
                "status", "OPEN"
        );
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());
    }
}
