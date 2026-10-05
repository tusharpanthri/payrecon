package com.tushar.payrecon.service;

import com.tushar.payrecon.dto.SeedResponse;
import com.tushar.payrecon.entity.Payment;
import com.tushar.payrecon.entity.Provider;
import com.tushar.payrecon.entity.ProviderTransaction;
import com.tushar.payrecon.repository.PaymentRepository;
import com.tushar.payrecon.repository.ProviderTransactionRepository;
import com.tushar.payrecon.repository.ReconciliationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates fake but realistic data so the engine has something to reconcile.
 * Every 8 payments cycle through every scenario, so every status shows up.
 */
@Service
public class DemoDataService {

    private final PaymentRepository paymentRepository;
    private final ProviderTransactionRepository providerTransactionRepository;
    private final ReconciliationRepository reconciliationRepository;

    public DemoDataService(PaymentRepository paymentRepository,
                           ProviderTransactionRepository providerTransactionRepository,
                           ReconciliationRepository reconciliationRepository) {
        this.paymentRepository = paymentRepository;
        this.providerTransactionRepository = providerTransactionRepository;
        this.reconciliationRepository = reconciliationRepository;
    }

    @Transactional
    public SeedResponse seed(int count) {
        // Reset. Order matters because reconciliations point at the other two tables.
        reconciliationRepository.deleteAllInBatch();
        providerTransactionRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();

        Random random = new Random(42); // fixed seed: same demo data every time
        LocalDate today = LocalDate.now();
        List<Payment> payments = new ArrayList<>();
        List<ProviderTransaction> transactions = new ArrayList<>();
        int externalCounter = 1;

        for (int i = 0; i < count; i++) {
            long amount = 1_000 + random.nextInt(49_000);              // $10.00 to $500.00
            String card = String.format("%04d", random.nextInt(10_000));
            LocalDate paidOn = today.minusDays(random.nextInt(10));
            LocalDate settledOn = paidOn.plusDays(random.nextInt(2)); // providers settle same day or next
            Payment payment = new Payment("INV-" + (1001 + i), amount, "USD", card, paidOn);
            payments.add(payment);

            switch (i % 8) {
                case 0, 1, 2 -> // Stripe, clean, fee taken  -> MATCHED_WITH_FEE
                        transactions.add(providerTx(Provider.STRIPE, externalCounter++, payment, settledOn, 0));
                case 3 ->       // PayPal, clean, fee taken  -> MATCHED_WITH_FEE
                        transactions.add(providerTx(Provider.PAYPAL, externalCounter++, payment, settledOn, 0));
                case 4 ->       // Bank, no fee, no reference, no card  -> MATCHED
                        transactions.add(new ProviderTransaction(Provider.BANK, id(Provider.BANK, externalCounter++),
                                null, amount, 0, "USD", null, settledOn));
                case 5 ->       // Stripe settled $5.00 short  -> AMOUNT_MISMATCH
                        transactions.add(providerTx(Provider.STRIPE, externalCounter++, payment, settledOn, 500));
                case 6 -> {     // Provider never reported it  -> MISSING_EXTERNAL
                }
                case 7 -> {     // PayPal reported it twice  -> MATCHED_WITH_FEE + DUPLICATE
                    transactions.add(providerTx(Provider.PAYPAL, externalCounter++, payment, settledOn, 0));
                    transactions.add(providerTx(Provider.PAYPAL, externalCounter++, payment, settledOn, 0));
                }
                default -> throw new IllegalStateException("unreachable");
            }
        }

        // Orphans: money the provider reports that we have no record of  -> MISSING_INTERNAL
        int orphans = Math.max(1, count / 10);
        for (int i = 0; i < orphans; i++) {
            long amount = 1_000 + random.nextInt(49_000);
            String card = String.format("%04d", random.nextInt(10_000));
            transactions.add(new ProviderTransaction(Provider.STRIPE, id(Provider.STRIPE, externalCounter++),
                    null, amount - stripeFee(amount), stripeFee(amount), "USD", card,
                    today.minusDays(random.nextInt(10))));
        }

        paymentRepository.saveAll(payments);
        providerTransactionRepository.saveAll(transactions);
        return new SeedResponse(payments.size(), transactions.size());
    }

    /** A provider record for a real payment. shortByMinor > 0 simulates the provider paying out less. */
    private ProviderTransaction providerTx(Provider provider, int counter, Payment p, LocalDate settledOn,
                                           long shortByMinor) {
        long fee = provider == Provider.PAYPAL ? paypalFee(p.getAmountMinor()) : stripeFee(p.getAmountMinor());
        long net = p.getAmountMinor() - fee - shortByMinor;
        return new ProviderTransaction(provider, id(provider, counter), p.getReference(), net, fee,
                p.getCurrency(), p.getCardLast4(), settledOn);
    }

    private static long stripeFee(long amountMinor) {
        return Math.round(amountMinor * 0.029) + 30;   // 2.9% + 30 cents
    }

    private static long paypalFee(long amountMinor) {
        return Math.round(amountMinor * 0.0349) + 49;  // 3.49% + 49 cents
    }

    private static String id(Provider provider, int counter) {
        return provider.idPrefix() + "_" + String.format("%06d", counter);
    }
}
