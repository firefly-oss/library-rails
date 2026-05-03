/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.dtos.reconciliation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractReconciliationController {

    protected final RailAdapter railAdapter;

    @GetMapping("/reconciliation/report")
    public Mono<ResponseEntity<ReconciliationReport>> getReconciliationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Getting reconciliation report for date: {}", date);
        return railAdapter.reconciliation().getReconciliationReport(date);
    }

    @PostMapping("/reconciliation/discrepancies")
    public Mono<ResponseEntity<List<DiscrepancyResponse>>> findDiscrepancies(@RequestBody ReconciliationRequest request) {
        log.debug("Finding discrepancies");
        return railAdapter.reconciliation().findDiscrepancies(request);
    }

    @GetMapping("/reconciliation/summary")
    public Mono<ResponseEntity<ReconciliationSummary>> getReconciliationSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Getting reconciliation summary from {} to {}", startDate, endDate);
        return railAdapter.reconciliation().getReconciliationSummary(startDate, endDate);
    }
}
