package com.tushar.payrecon.entity;

/** Where an external transaction came from. The prefix is used for demo external ids. */
public enum Provider {
    STRIPE("ch"),
    PAYPAL("pp"),
    BANK("bt");

    private final String idPrefix;

    Provider(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public String idPrefix() {
        return idPrefix;
    }
}
