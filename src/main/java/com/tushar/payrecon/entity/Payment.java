package com.tushar.payrecon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** A payment as recorded in our own (internal) ledger. */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false)
    private String currency;

    @Column(name = "card_last4")
    private String cardLast4;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /** JPA needs a no-arg constructor. Protected so application code cannot use it. */
    protected Payment() {
    }

    public Payment(String reference, long amountMinor, String currency, String cardLast4, LocalDate paymentDate) {
        this.reference = reference;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.cardLast4 = cardLast4;
        this.paymentDate = paymentDate;
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public long getAmountMinor() { return amountMinor; }
    public String getCurrency() { return currency; }
    public String getCardLast4() { return cardLast4; }
    public LocalDate getPaymentDate() { return paymentDate; }
}
