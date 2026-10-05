package com.tushar.payrecon.dto;

import com.tushar.payrecon.entity.ReconStatus;

import java.util.List;

/**
 * matchRatePercent = internal payments that matched cleanly (with or without a fee) / all internal payments.
 */
public record SummaryResponse(long totalResults, long totalPayments, int matchRatePercent,
                              List<StatusCount> byStatus) {

    public record StatusCount(ReconStatus status, long count, long differenceMinor) {
    }
}
