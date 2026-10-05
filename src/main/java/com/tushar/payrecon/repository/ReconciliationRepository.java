package com.tushar.payrecon.repository;

import com.tushar.payrecon.entity.ReconStatus;
import com.tushar.payrecon.entity.Reconciliation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReconciliationRepository extends JpaRepository<Reconciliation, Long> {

    // @EntityGraph loads payment and providerTransaction in the SAME query (a join).
    // Without it, listing 20 results would fire 1 + 20 + 20 queries: the N+1 problem.
    @EntityGraph(attributePaths = {"payment", "providerTransaction"})
    Page<Reconciliation> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"payment", "providerTransaction"})
    Page<Reconciliation> findByStatus(ReconStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"payment", "providerTransaction"})
    Optional<Reconciliation> findWithDetailsById(Long id);

    // Aggregation happens in the database, not in Java. Text block is a Java 15+ feature.
    @Query("""
            select r.status as status,
                   count(r) as total,
                   sum(r.differenceMinor) as differenceMinor
            from Reconciliation r
            group by r.status
            """)
    List<StatusCount> countByStatus();

    /** Interface projection: Spring fills these getters from the query aliases above. */
    interface StatusCount {
        ReconStatus getStatus();
        Long getTotal();
        Long getDifferenceMinor();
    }
}
