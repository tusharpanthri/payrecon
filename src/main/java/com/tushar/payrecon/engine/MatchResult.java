package com.tushar.payrecon.engine;

import com.tushar.payrecon.entity.Payment;
import com.tushar.payrecon.entity.ProviderTransaction;
import com.tushar.payrecon.entity.ReconStatus;

/**
 * Output of the engine for one payment, one provider transaction, or a pair.
 * differenceMinor is the money that is not accounted for (0 for a clean match).
 */
public record MatchResult(
        Payment payment,
        ProviderTransaction providerTransaction,
        ReconStatus status,
        int confidence,
        long differenceMinor) {
}
