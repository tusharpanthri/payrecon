package com.tushar.payrecon.controller;

import com.tushar.payrecon.dto.PageResponse;
import com.tushar.payrecon.dto.ReconciliationResponse;
import com.tushar.payrecon.dto.SummaryResponse;
import com.tushar.payrecon.entity.ReconStatus;
import com.tushar.payrecon.service.ReconciliationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reconciliations")
public class ReconciliationController {

    private final ReconciliationService service;

    public ReconciliationController(ReconciliationService service) {
        this.service = service;
    }

    @PostMapping("/run")
    public SummaryResponse run() {
        return service.run();
    }

    // GET /api/reconciliations?status=AMOUNT_MISMATCH&page=0&size=20
    @GetMapping
    public PageResponse<ReconciliationResponse> list(
            @RequestParam(required = false) ReconStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.list(status, pageable);
    }

    // Declared before /{id} only for readability; Spring matches the literal path "summary" first anyway.
    @GetMapping("/summary")
    public SummaryResponse summary() {
        return service.summary();
    }

    @GetMapping("/{id}")
    public ReconciliationResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
