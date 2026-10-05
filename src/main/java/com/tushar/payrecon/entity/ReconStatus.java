package com.tushar.payrecon.entity;

public enum ReconStatus {
    MATCHED,            // amounts agree, no fee
    MATCHED_WITH_FEE,   // amounts agree once the provider fee is added back
    AMOUNT_MISMATCH,    // same payment, but the money does not add up
    MISSING_EXTERNAL,   // we recorded a payment, no provider record exists
    MISSING_INTERNAL,   // provider reports money we have no record of
    DUPLICATE;          // provider reported the same payment more than once

    /** Java 17 switch expression. */
    public boolean isMatched() {
        return switch (this) {
            case MATCHED, MATCHED_WITH_FEE -> true;
            default -> false;
        };
    }
}
