package com.tushar.payrecon.dto;

import com.tushar.payrecon.entity.Provider;
import com.tushar.payrecon.entity.ProviderTransaction;

import java.time.LocalDate;

public record ProviderTransactionResponse(Long id, Provider provider, String externalId, String reference,
                                          long amountMinor, long feeMinor, String currency, String cardLast4,
                                          LocalDate transactionDate) {

    public static ProviderTransactionResponse from(ProviderTransaction t) {
        return new ProviderTransactionResponse(t.getId(), t.getProvider(), t.getExternalId(), t.getReference(),
                t.getAmountMinor(), t.getFeeMinor(), t.getCurrency(), t.getCardLast4(), t.getTransactionDate());
    }
}
