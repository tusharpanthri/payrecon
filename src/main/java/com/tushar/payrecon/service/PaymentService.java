package com.tushar.payrecon.service;

import com.tushar.payrecon.dto.CreatePaymentRequest;
import com.tushar.payrecon.dto.PageResponse;
import com.tushar.payrecon.dto.PaymentResponse;
import com.tushar.payrecon.entity.Payment;
import com.tushar.payrecon.exception.ConflictException;
import com.tushar.payrecon.exception.NotFoundException;
import com.tushar.payrecon.repository.PaymentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // Constructor injection: Spring passes in the repository bean. No @Autowired needed with one constructor.
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        if (paymentRepository.existsByReference(request.reference())) {
            throw new ConflictException("Payment with reference " + request.reference() + " already exists");
        }
        Payment saved = paymentRepository.save(new Payment(request.reference(), request.amountMinor(),
                request.currency(), request.cardLast4(), request.paymentDate()));
        return PaymentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> list(Pageable pageable) {
        return PageResponse.from(paymentRepository.findAll(pageable).map(PaymentResponse::from));
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new NotFoundException("Payment " + id + " not found"));
    }
}
