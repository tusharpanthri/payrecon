package com.tushar.payrecon.dto;

import com.tushar.payrecon.entity.Payment;
import com.tushar.payrecon.entity.Provider;
import com.tushar.payrecon.entity.ProviderTransaction;
import com.tushar.payrecon.entity.ReconStatus;
import com.tushar.payrecon.entity.Reconciliation;

/** Flat view of one result. Payment fields are null for MISSING_INTERNAL, provider fields for MISSING_EXTERNAL. */
public record ReconciliationResponse(
        Long id,
        ReconStatus status,
        int confidence,
        long differenceMinor,
        Long paymentId,
        String paymentReference,
        Long paymentAmountMinor,
        Long providerTransactionId,
        Provider provider,
        String externalId,
        Long providerAmountMinor,
        Long providerFeeMinor) {

    public static ReconciliationResponse from(Reconciliation r) {
        Payment p = r.getPayment();
        ProviderTransaction t = r.getProviderTransaction();
        return new ReconciliationResponse(
                r.getId(),
                r.getStatus(),
                r.getConfidence(),
                r.getDifferenceMinor(),
                p == null ? null : p.getId(),
                p == null ? null : p.getReference(),
                p == null ? null : p.getAmountMinor(),
                t == null ? null : t.getId(),
                t == null ? null : t.getProvider(),
                t == null ? null : t.getExternalId(),
                t == null ? null : t.getAmountMinor(),
                t == null ? null : t.getFeeMinor());
    }
}
