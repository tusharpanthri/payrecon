package com.tushar.payrecon.service;

import com.tushar.payrecon.dto.PageResponse;
import com.tushar.payrecon.dto.ReconciliationResponse;
import com.tushar.payrecon.dto.SummaryResponse;
import com.tushar.payrecon.engine.MatchResult;
import com.tushar.payrecon.engine.MatchingEngine;
import com.tushar.payrecon.entity.ReconStatus;
import com.tushar.payrecon.entity.Reconciliation;
import com.tushar.payrecon.exception.NotFoundException;
import com.tushar.payrecon.repository.PaymentRepository;
import com.tushar.payrecon.repository.ProviderTransactionRepository;
import com.tushar.payrecon.repository.ReconciliationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReconciliationService {

    private final PaymentRepository paymentRepository;
    private final ProviderTransactionRepository providerTransactionRepository;
    private final ReconciliationRepository reconciliationRepository;
    private final MatchingEngine matchingEngine;

    public ReconciliationService(PaymentRepository paymentRepository,
                                 ProviderTransactionRepository providerTransactionRepository,
                                 ReconciliationRepository reconciliationRepository,
                                 MatchingEngine matchingEngine) {
        this.paymentRepository = paymentRepository;
        this.providerTransactionRepository = providerTransactionRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.matchingEngine = matchingEngine;
    }

    /**
     * Wipes old results and reconciles everything again.
     * One transaction: if saving fails halfway, the old results come back (rollback), never a half-written run.
     */
    @Transactional
    public SummaryResponse run() {
        reconciliationRepository.deleteAllInBatch();

        List<MatchResult> results = matchingEngine.reconcile(
                paymentRepository.findAll(),
                providerTransactionRepository.findAll());

        reconciliationRepository.saveAll(results.stream().map(Reconciliation::from).toList());
        return summary();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReconciliationResponse> list(ReconStatus status, Pageable pageable) {
        Page<Reconciliation> page = (status == null)
                ? reconciliationRepository.findAll(pageable)
                : reconciliationRepository.findByStatus(status, pageable);
        return PageResponse.from(page.map(ReconciliationResponse::from));
    }

    @Transactional(readOnly = true)
    public ReconciliationResponse get(Long id) {
        return reconciliationRepository.findWithDetailsById(id)
                .map(ReconciliationResponse::from)
                .orElseThrow(() -> new NotFoundException("Reconciliation " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        List<SummaryResponse.StatusCount> byStatus = reconciliationRepository.countByStatus().stream()
                .map(row -> new SummaryResponse.StatusCount(row.getStatus(), row.getTotal(), row.getDifferenceMinor()))
                .toList();

        long totalResults = byStatus.stream().mapToLong(SummaryResponse.StatusCount::count).sum();
        long matched = byStatus.stream()
                .filter(s -> s.status().isMatched())
                .mapToLong(SummaryResponse.StatusCount::count)
                .sum();
        long totalPayments = paymentRepository.count();
        int matchRate = totalPayments == 0 ? 0 : (int) (matched * 100 / totalPayments);

        return new SummaryResponse(totalResults, totalPayments, matchRate, byStatus);
    }
}
