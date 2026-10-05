package com.tushar.payrecon.engine;

import com.tushar.payrecon.entity.Payment;
import com.tushar.payrecon.entity.ProviderTransaction;
import com.tushar.payrecon.entity.ReconStatus;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Pure matching logic. No database, no HTTP. That is why it can be unit tested with plain "new".
 *
 * Scoring: each field that both records have is worth some points. The score is
 * points earned / points possible, as a percent. A pair at or above THRESHOLD is a candidate match.
 */
@Component
public class MatchingEngine {

    public static final int THRESHOLD = 65;

    private static final int W_REFERENCE = 40;
    private static final int W_AMOUNT = 30;
    private static final int W_CURRENCY = 10;
    private static final int W_CARD = 10;
    private static final int W_DATE = 10;
    private static final int DATE_TOLERANCE_DAYS = 3;

    /** A scored payment / transaction pair. */
    private record Candidate(Payment payment, ProviderTransaction tx, int score) {
    }

    public List<MatchResult> reconcile(List<Payment> payments, List<ProviderTransaction> transactions) {

        // 1. Score every pair. Keep only pairs at or above the threshold.
        //    O(payments x transactions): fine for a demo, see README for how to scale it.
        List<Candidate> candidates = new ArrayList<>();
        for (ProviderTransaction tx : transactions) {
            for (Payment payment : payments) {
                int score = score(payment, tx);
                if (score >= THRESHOLD) {
                    candidates.add(new Candidate(payment, tx, score));
                }
            }
        }

        // 2. Greedy assignment: best scores first, each record used at most once.
        candidates.sort(Comparator.comparingInt(Candidate::score).reversed());
        Set<Payment> usedPayments = new HashSet<>();
        Set<ProviderTransaction> usedTransactions = new HashSet<>();
        List<MatchResult> results = new ArrayList<>();

        for (Candidate c : candidates) {
            if (usedPayments.contains(c.payment()) || usedTransactions.contains(c.tx())) {
                continue;
            }
            results.add(classify(c));
            usedPayments.add(c.payment());
            usedTransactions.add(c.tx());
        }

        // 3. Leftover provider transactions.
        //    If it looked like one of our payments, but that payment is already taken, it is a duplicate.
        //    If it looked like nothing, we have no record of this money.
        for (ProviderTransaction tx : transactions) {
            if (usedTransactions.contains(tx)) {
                continue;
            }
            Optional<Candidate> best = candidates.stream()
                    .filter(c -> c.tx() == tx)
                    .findFirst(); // list is sorted, so the first one is the best
            if (best.isPresent()) {
                results.add(new MatchResult(best.get().payment(), tx, ReconStatus.DUPLICATE,
                        best.get().score(), tx.grossAmountMinor()));
            } else {
                results.add(new MatchResult(null, tx, ReconStatus.MISSING_INTERNAL, 0, tx.grossAmountMinor()));
            }
        }

        // 4. Leftover internal payments: the provider never reported them.
        for (Payment payment : payments) {
            if (!usedPayments.contains(payment)) {
                results.add(new MatchResult(payment, null, ReconStatus.MISSING_EXTERNAL, 0, payment.getAmountMinor()));
            }
        }

        return results;
    }

    /** Percent score (0 to 100). Fields missing on either side are left out of the total. */
    int score(Payment payment, ProviderTransaction tx) {
        int earned = 0;
        int possible = 0;

        if (payment.getReference() != null && tx.getReference() != null) {
            possible += W_REFERENCE;
            if (payment.getReference().equals(tx.getReference())) {
                earned += W_REFERENCE;
            }
        }

        possible += W_AMOUNT;
        if (payment.getAmountMinor() == tx.grossAmountMinor()) {
            earned += W_AMOUNT;
        }

        possible += W_CURRENCY;
        if (payment.getCurrency().equals(tx.getCurrency())) {
            earned += W_CURRENCY;
        }

        if (payment.getCardLast4() != null && tx.getCardLast4() != null) {
            possible += W_CARD;
            if (payment.getCardLast4().equals(tx.getCardLast4())) {
                earned += W_CARD;
            }
        }

        possible += W_DATE;
        long days = Math.abs(ChronoUnit.DAYS.between(payment.getPaymentDate(), tx.getTransactionDate()));
        if (days == 0) {
            earned += W_DATE;
        } else if (days <= DATE_TOLERANCE_DAYS) {
            earned += W_DATE / 2;
        }

        return earned * 100 / possible;
    }

    /** Decide what kind of match a winning pair is. */
    private MatchResult classify(Candidate c) {
        long difference = c.payment().getAmountMinor() - c.tx().grossAmountMinor();
        ReconStatus status;
        if (difference != 0) {
            status = ReconStatus.AMOUNT_MISMATCH;
        } else if (c.tx().getFeeMinor() > 0) {
            status = ReconStatus.MATCHED_WITH_FEE;
        } else {
            status = ReconStatus.MATCHED;
        }
        return new MatchResult(c.payment(), c.tx(), status, c.score(), difference);
    }
}
