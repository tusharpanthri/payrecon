package com.tushar.payrecon.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full stack test: HTTP -> controller -> service -> JPA -> H2 database (schema created by Flyway).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReconciliationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seedThenRunProducesEveryStatus() throws Exception {
        // 16 payments = two full cycles of the 8 demo scenarios, plus 1 orphan provider record
        mockMvc.perform(post("/api/demo/seed").param("count", "16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments").value(16))
                .andExpect(jsonPath("$.providerTransactions").value(17));

        mockMvc.perform(post("/api/reconciliations/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayments").value(16))
                .andExpect(jsonPath("$.totalResults").value(19))
                .andExpect(jsonPath("$.matchRatePercent").value(75))
                .andExpect(jsonPath("$.byStatus[?(@.status=='MATCHED_WITH_FEE')].count", contains(10)))
                .andExpect(jsonPath("$.byStatus[?(@.status=='MATCHED')].count", contains(2)))
                .andExpect(jsonPath("$.byStatus[?(@.status=='AMOUNT_MISMATCH')].count", contains(2)))
                .andExpect(jsonPath("$.byStatus[?(@.status=='MISSING_EXTERNAL')].count", contains(2)))
                .andExpect(jsonPath("$.byStatus[?(@.status=='DUPLICATE')].count", contains(2)))
                .andExpect(jsonPath("$.byStatus[?(@.status=='MISSING_INTERNAL')].count", contains(1)));

        mockMvc.perform(get("/api/reconciliations").param("status", "AMOUNT_MISMATCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].differenceMinor", everyItem(is(500))));
    }

    @Test
    void createPaymentValidatesInput() throws Exception {
        String bad = """
                {"reference": "", "amountMinor": -5, "currency": "usd", "paymentDate": "2026-10-01"}
                """;
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateReferenceReturnsConflict() throws Exception {
        String body = """
                {"reference": "DUP-1", "amountMinor": 5000, "currency": "USD",
                 "cardLast4": "4242", "paymentDate": "2026-10-01"}
                """;
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/reconciliations/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void seedCountMustBePositive() throws Exception {
        mockMvc.perform(post("/api/demo/seed").param("count", "0"))
                .andExpect(status().isBadRequest());
    }
}
