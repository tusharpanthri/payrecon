package com.tushar.payrecon.entity;

import com.tushar.payrecon.engine.MatchResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One result of a reconciliation run. Either side can be null (missing records). */
@Entity
@Table(name = "reconciliations")
public class Reconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY: do not load the payment unless we actually touch it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_transaction_id")
    private ProviderTransaction providerTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconStatus status;

    @Column(nullable = false)
    private int confidence;

    @Column(name = "difference_minor", nullable = false)
    private long differenceMinor;

    protected Reconciliation() {
    }

    public Reconciliation(Payment payment, ProviderTransaction providerTransaction, ReconStatus status,
                          int confidence, long differenceMinor) {
        this.payment = payment;
        this.providerTransaction = providerTransaction;
        this.status = status;
        this.confidence = confidence;
        this.differenceMinor = differenceMinor;
    }

    public static Reconciliation from(MatchResult result) {
        return new Reconciliation(result.payment(), result.providerTransaction(), result.status(),
                result.confidence(), result.differenceMinor());
    }

    public Long getId() { return id; }
    public Payment getPayment() { return payment; }
    public ProviderTransaction getProviderTransaction() { return providerTransaction; }
    public ReconStatus getStatus() { return status; }
    public int getConfidence() { return confidence; }
    public long getDifferenceMinor() { return differenceMinor; }
}
