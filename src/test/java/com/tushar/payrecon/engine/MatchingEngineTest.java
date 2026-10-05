package com.tushar.payrecon.engine;

import com.tushar.payrecon.entity.Payment;
import com.tushar.payrecon.entity.Provider;
import com.tushar.payrecon.entity.ProviderTransaction;
import com.tushar.payrecon.entity.ReconStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests. No Spring context, no database: the engine is just Java. */
class MatchingEngineTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    private final MatchingEngine engine = new MatchingEngine();

    private Payment payment(String ref, long amount) {
        return new Payment(ref, amount, "USD", "4242", DAY);
    }

    private ProviderTransaction stripe(String externalId, String ref, long net, long fee) {
        return new ProviderTransaction(Provider.STRIPE, externalId, ref, net, fee, "USD", "4242", DAY);
    }

    @Test
    void cleanMatchWithFee() {
        Payment p = payment("INV-1", 10_000);
        ProviderTransaction tx = stripe("ch_1", "INV-1", 9_680, 320);

        List<MatchResult> results = engine.reconcile(List.of(p), List.of(tx));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconStatus.MATCHED_WITH_FEE);
        assertThat(results.get(0).confidence()).isEqualTo(100);
        assertThat(results.get(0).differenceMinor()).isZero();
    }

    @Test
    void bankTransferWithoutReferenceStillMatchesOnAmountAndDate() {
        Payment p = payment("INV-1", 25_000);
        ProviderTransaction bank = new ProviderTransaction(Provider.BANK, "bt_1", null, 25_000, 0, "USD", null, DAY);

        List<MatchResult> results = engine.reconcile(List.of(p), List.of(bank));

        assertThat(results).singleElement()
                .extracting(MatchResult::status)
                .isEqualTo(ReconStatus.MATCHED);
    }

    @Test
    void sameReferenceButWrongAmountIsAmountMismatch() {
        Payment p = payment("INV-1", 10_000);
        ProviderTransaction tx = stripe("ch_1", "INV-1", 9_180, 320); // $5.00 short

        MatchResult result = engine.reconcile(List.of(p), List.of(tx)).get(0);

        assertThat(result.status()).isEqualTo(ReconStatus.AMOUNT_MISMATCH);
        assertThat(result.differenceMinor()).isEqualTo(500);
        assertThat(result.confidence()).isGreaterThanOrEqualTo(MatchingEngine.THRESHOLD);
    }

    @Test
    void paymentWithNoProviderRecordIsMissingExternal() {
        MatchResult result = engine.reconcile(List.of(payment("INV-1", 10_000)), List.of()).get(0);

        assertThat(result.status()).isEqualTo(ReconStatus.MISSING_EXTERNAL);
        assertThat(result.providerTransaction()).isNull();
        assertThat(result.differenceMinor()).isEqualTo(10_000);
    }

    @Test
    void unknownProviderRecordIsMissingInternal() {
        ProviderTransaction orphan = new ProviderTransaction(Provider.STRIPE, "ch_9", null, 7_777, 0,
                "USD", "1111", DAY.minusDays(8));

        MatchResult result = engine.reconcile(List.of(payment("INV-1", 10_000)), List.of(orphan)).stream()
                .filter(r -> r.providerTransaction() == orphan)
                .findFirst()
                .orElseThrow();

        assertThat(result.status()).isEqualTo(ReconStatus.MISSING_INTERNAL);
        assertThat(result.payment()).isNull();
    }

    @Test
    void sameTransactionReportedTwiceGivesOneMatchAndOneDuplicate() {
        Payment p = payment("INV-1", 10_000);
        ProviderTransaction first = stripe("ch_1", "INV-1", 9_680, 320);
        ProviderTransaction second = stripe("ch_2", "INV-1", 9_680, 320);

        List<ReconStatus> statuses = engine.reconcile(List.of(p), List.of(first, second)).stream()
                .map(MatchResult::status)
                .toList();

        assertThat(statuses).containsExactlyInAnyOrder(ReconStatus.MATCHED_WITH_FEE, ReconStatus.DUPLICATE);
    }

    @Test
    void eachPaymentGoesToItsBestTransaction() {
        Payment a = payment("INV-1", 10_000);
        Payment b = payment("INV-2", 20_000);
        ProviderTransaction txB = stripe("ch_2", "INV-2", 19_390, 610);
        ProviderTransaction txA = stripe("ch_1", "INV-1", 9_680, 320);

        List<MatchResult> results = engine.reconcile(List.of(a, b), List.of(txB, txA));

        assertThat(results).hasSize(2).allMatch(r -> r.status() == ReconStatus.MATCHED_WITH_FEE);
        assertThat(results).anyMatch(r -> r.payment() == a && r.providerTransaction() == txA);
        assertThat(results).anyMatch(r -> r.payment() == b && r.providerTransaction() == txB);
    }
}
