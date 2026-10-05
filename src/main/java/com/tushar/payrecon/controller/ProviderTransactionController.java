package com.tushar.payrecon.controller;

import com.tushar.payrecon.dto.CreateProviderTransactionRequest;
import com.tushar.payrecon.dto.PageResponse;
import com.tushar.payrecon.dto.ProviderTransactionResponse;
import com.tushar.payrecon.service.ProviderTransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provider-transactions")
public class ProviderTransactionController {

    private final ProviderTransactionService service;

    public ProviderTransactionController(ProviderTransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderTransactionResponse create(@Valid @RequestBody CreateProviderTransactionRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<ProviderTransactionResponse> list(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(pageable);
    }
}
