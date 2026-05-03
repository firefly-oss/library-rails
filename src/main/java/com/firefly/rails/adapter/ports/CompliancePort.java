/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.adapter.ports;

import com.firefly.rails.dtos.compliance.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

/**
 * Port interface for compliance and regulatory checks.
 * 
 * Handles AML (Anti-Money Laundering), KYC (Know Your Customer),
 * sanctions screening, and other regulatory requirements.
 */
public interface CompliancePort {

    /**
     * Perform AML/KYC check on a payment.
     * Screens against sanctions lists, PEP lists, and checks transaction patterns.
     *
     * @param request compliance check request
     * @return reactive publisher with compliance result
     */
    Mono<ResponseEntity<ComplianceCheckResponse>> performComplianceCheck(ComplianceCheckRequest request);

    /**
     * Screen a beneficiary against sanctions lists.
     * Checks OFAC, UN, EU sanctions lists.
     *
     * @param request sanctions screening request
     * @return reactive publisher with screening result
     */
    Mono<ResponseEntity<SanctionsScreeningResponse>> screenSanctions(SanctionsScreeningRequest request);

    /**
     * Verify customer identity (KYC).
     * Validates customer information and documents.
     *
     * @param request KYC verification request
     * @return reactive publisher with verification result
     */
    Mono<ResponseEntity<KYCVerificationResponse>> verifyKYC(KYCVerificationRequest request);

    /**
     * Check if transaction requires enhanced due diligence.
     * Based on amount, destination, customer risk profile.
     *
     * @param request due diligence check request
     * @return reactive publisher with due diligence result
     */
    Mono<ResponseEntity<DueDiligenceResponse>> checkDueDiligence(DueDiligenceRequest request);

    /**
     * Report suspicious activity (SAR - Suspicious Activity Report).
     * Required for regulatory compliance.
     *
     * @param request suspicious activity report
     * @return reactive publisher with report confirmation
     */
    Mono<ResponseEntity<SARResponse>> reportSuspiciousActivity(SARRequest request);

    /**
     * Get customer risk profile.
     * Returns risk level based on transaction history and profile.
     *
     * @param customerId customer identifier
     * @return reactive publisher with risk profile
     */
    Mono<ResponseEntity<RiskProfile>> getCustomerRiskProfile(String customerId);
}
