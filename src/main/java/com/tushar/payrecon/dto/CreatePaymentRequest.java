package com.tushar.payrecon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Request body for POST /api/payments. Validated by @Valid in the controller. */
public record CreatePaymentRequest(
        @NotBlank String reference,
        @Positive long amountMinor,
        @NotNull @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO code like USD") String currency,
        @Pattern(regexp = "\\d{4}", message = "must be 4 digits") String cardLast4,
        @NotNull LocalDate paymentDate) {
}
