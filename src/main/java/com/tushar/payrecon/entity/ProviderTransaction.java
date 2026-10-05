package com.tushar.payrecon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * A transaction as reported by Stripe, PayPal or a bank.
 * amountMinor is the NET amount that settled; feeMinor is what the provider kept.
 * So the gross amount the customer paid is amountMinor + feeMinor.
 */
@Entity
@Table(name = "provider_transactions")
public class ProviderTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    /** Our invoice reference, if the provider passed it through. Banks often do not. */
    private String reference;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "fee_minor", nullable = false)
    private long feeMinor;

    @Column(nullable = false)
    private String currency;

    @Column(name = "card_last4")
    private String cardLast4;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    protected ProviderTransaction() {
    }

    public ProviderTransaction(Provider provider, String externalId, String reference, long amountMinor,
                               long feeMinor, String currency, String cardLast4, LocalDate transactionDate) {
        this.provider = provider;
        this.externalId = externalId;
        this.reference = reference;
        this.amountMinor = amountMinor;
        this.feeMinor = feeMinor;
        this.currency = currency;
        this.cardLast4 = cardLast4;
        this.transactionDate = transactionDate;
    }

    public long grossAmountMinor() {
        return amountMinor + feeMinor;
    }

    public Long getId() { return id; }
    public Provider getProvider() { return provider; }
    public String getExternalId() { return externalId; }
    public String getReference() { return reference; }
    public long getAmountMinor() { return amountMinor; }
    public long getFeeMinor() { return feeMinor; }
    public String getCurrency() { return currency; }
    public String getCardLast4() { return cardLast4; }
    public LocalDate getTransactionDate() { return transactionDate; }
}
