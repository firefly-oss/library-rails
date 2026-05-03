/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.dtos.compliance.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractComplianceController {

    protected final RailAdapter railAdapter;

    @PostMapping("/compliance/check")
    public Mono<ResponseEntity<ComplianceCheckResponse>> performComplianceCheck(@RequestBody ComplianceCheckRequest request) {
        log.info("Performing compliance check");
        return railAdapter.compliance().performComplianceCheck(request);
    }

    @PostMapping("/compliance/sanctions")
    public Mono<ResponseEntity<SanctionsScreeningResponse>> screenSanctions(@RequestBody SanctionsScreeningRequest request) {
        log.info("Screening sanctions");
        return railAdapter.compliance().screenSanctions(request);
    }

    @PostMapping("/compliance/kyc")
    public Mono<ResponseEntity<KYCVerificationResponse>> verifyKYC(@RequestBody KYCVerificationRequest request) {
        log.info("Verifying KYC");
        return railAdapter.compliance().verifyKYC(request);
    }

    @PostMapping("/compliance/due-diligence")
    public Mono<ResponseEntity<DueDiligenceResponse>> checkDueDiligence(@RequestBody DueDiligenceRequest request) {
        log.info("Checking due diligence");
        return railAdapter.compliance().checkDueDiligence(request);
    }

    @PostMapping("/compliance/sar")
    public Mono<ResponseEntity<SARResponse>> reportSuspiciousActivity(@RequestBody SARRequest request) {
        log.warn("Reporting suspicious activity");
        return railAdapter.compliance().reportSuspiciousActivity(request);
    }

    @GetMapping("/compliance/risk-profile/{customerId}")
    public Mono<ResponseEntity<RiskProfile>> getCustomerRiskProfile(@PathVariable String customerId) {
        log.debug("Getting customer risk profile: {}", customerId);
        return railAdapter.compliance().getCustomerRiskProfile(customerId);
    }
}
