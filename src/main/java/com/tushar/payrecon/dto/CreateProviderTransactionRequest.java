package com.tushar.payrecon.dto;

import com.tushar.payrecon.entity.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record CreateProviderTransactionRequest(
        @NotNull Provider provider,
        @NotBlank String externalId,
        String reference,
        @Positive long amountMinor,
        @PositiveOrZero long feeMinor,
        @NotNull @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO code like USD") String currency,
        @Pattern(regexp = "\\d{4}", message = "must be 4 digits") String cardLast4,
        @NotNull LocalDate transactionDate) {
}
