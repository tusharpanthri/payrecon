package com.tushar.payrecon.controller;

import com.tushar.payrecon.dto.CreatePaymentRequest;
import com.tushar.payrecon.dto.PageResponse;
import com.tushar.payrecon.dto.PaymentResponse;
import com.tushar.payrecon.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Controllers stay thin: parse HTTP, call the service, return. No business logic here. */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.create(request);
    }

    // GET /api/payments?page=0&size=20
    @GetMapping
    public PageResponse<PaymentResponse> list(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                                              Pageable pageable) {
        return paymentService.list(pageable);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return paymentService.get(id);
    }
}
