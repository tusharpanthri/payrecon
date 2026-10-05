package com.tushar.payrecon.repository;

import com.tushar.payrecon.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Spring Data writes the SQL from the method name.
    boolean existsByReference(String reference);
}
