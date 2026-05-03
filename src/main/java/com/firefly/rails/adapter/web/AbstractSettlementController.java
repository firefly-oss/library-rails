/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.dtos.settlement.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstract REST controller for settlement operations.
 * 
 * Provides endpoints for:
 * - Settlement reports
 * - Settlement details
 * - Expected settlement dates
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSettlementController {

    protected final RailAdapter railAdapter;

    /**
     * Get settlement report for a specific date.
     * GET /settlement/report?date=2024-01-15
     */
    @GetMapping("/settlement/report")
    public Mono<ResponseEntity<SettlementReport>> getSettlementReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Getting settlement report for date: {}", date);
        return railAdapter.settlement().getSettlementReport(date);
    }

    /**
     * Get settlement reports for a date range.
     * POST /settlement/reports
     */
    @PostMapping("/settlement/reports")
    public Mono<ResponseEntity<List<SettlementReport>>> getSettlementReports(
            @RequestBody SettlementReportRequest request) {
        log.debug("Getting settlement reports for date range");
        return railAdapter.settlement().getSettlementReports(request);
    }

    /**
     * Get detailed settlement transactions.
     * GET /settlement/{settlementId}
     */
    @GetMapping("/settlement/{settlementId}")
    public Mono<ResponseEntity<SettlementDetails>> getSettlementDetails(@PathVariable String settlementId) {
        log.debug("Getting settlement details: {}", settlementId);
        return railAdapter.settlement().getSettlementDetails(settlementId);
    }

    /**
     * Get expected settlement date for a payment.
     * GET /settlement/expected/{paymentId}
     */
    @GetMapping("/settlement/expected/{paymentId}")
    public Mono<ResponseEntity<SettlementDateResponse>> getExpectedSettlementDate(@PathVariable String paymentId) {
        log.debug("Getting expected settlement date for payment: {}", paymentId);
        return railAdapter.settlement().getExpectedSettlementDate(paymentId);
    }
}
