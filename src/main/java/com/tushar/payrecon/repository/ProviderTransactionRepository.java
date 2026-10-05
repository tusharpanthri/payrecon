package com.tushar.payrecon.repository;

import com.tushar.payrecon.entity.ProviderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderTransactionRepository extends JpaRepository<ProviderTransaction, Long> {

    boolean existsByExternalId(String externalId);
}
