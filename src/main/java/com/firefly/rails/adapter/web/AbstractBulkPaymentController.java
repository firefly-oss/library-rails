/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.dtos.bulk.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBulkPaymentController {

    protected final RailAdapter railAdapter;

    @PostMapping("/bulk/submit")
    public Mono<ResponseEntity<BulkPaymentResponse>> submitBulkPayment(@RequestBody BulkPaymentRequest request) {
        log.info("Submitting bulk payment");
        return railAdapter.bulkPayments().submitBulkPayment(request);
    }

    @GetMapping("/bulk/{bulkPaymentId}/status")
    public Mono<ResponseEntity<BulkPaymentStatusResponse>> getBulkPaymentStatus(@PathVariable String bulkPaymentId) {
        log.debug("Getting bulk payment status: {}", bulkPaymentId);
        return railAdapter.bulkPayments().getBulkPaymentStatus(bulkPaymentId);
    }

    @DeleteMapping("/bulk/{bulkPaymentId}")
    public Mono<ResponseEntity<BulkPaymentResponse>> cancelBulkPayment(@PathVariable String bulkPaymentId) {
        log.info("Cancelling bulk payment: {}", bulkPaymentId);
        return railAdapter.bulkPayments().cancelBulkPayment(bulkPaymentId);
    }
}
