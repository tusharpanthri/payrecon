package com.tushar.payrecon.dto;

import com.tushar.payrecon.entity.Payment;

import java.time.LocalDate;

public record PaymentResponse(Long id, String reference, long amountMinor, String currency,
                              String cardLast4, LocalDate paymentDate) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getReference(), p.getAmountMinor(), p.getCurrency(),
                p.getCardLast4(), p.getPaymentDate());
    }
}
