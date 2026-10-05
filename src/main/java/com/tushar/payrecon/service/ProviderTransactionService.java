package com.tushar.payrecon.service;

import com.tushar.payrecon.dto.CreateProviderTransactionRequest;
import com.tushar.payrecon.dto.PageResponse;
import com.tushar.payrecon.dto.ProviderTransactionResponse;
import com.tushar.payrecon.entity.ProviderTransaction;
import com.tushar.payrecon.exception.ConflictException;
import com.tushar.payrecon.repository.ProviderTransactionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderTransactionService {

    private final ProviderTransactionRepository repository;

    public ProviderTransactionService(ProviderTransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProviderTransactionResponse create(CreateProviderTransactionRequest r) {
        if (repository.existsByExternalId(r.externalId())) {
            throw new ConflictException("Provider transaction " + r.externalId() + " already exists");
        }
        ProviderTransaction saved = repository.save(new ProviderTransaction(r.provider(), r.externalId(),
                r.reference(), r.amountMinor(), r.feeMinor(), r.currency(), r.cardLast4(), r.transactionDate()));
        return ProviderTransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProviderTransactionResponse> list(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(ProviderTransactionResponse::from));
    }
}
