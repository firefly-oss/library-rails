/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.dtos.payments.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Abstract REST controller for payment rail operations.
 * 
 * Implementations inherit complete REST API with all payment operations including:
 * - Pre-flight validation and simulation
 * - Two-phase commit (authorize + confirm) with SCA support
 * - Direct single-phase execution
 * - Query operations (get, list, status)
 * - Cancellation and returns/refunds
 * 
 * All endpoints support reactive processing with Project Reactor.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentRailController {

    protected final RailAdapter railAdapter;

    // ==================== VALIDATION & SIMULATION ====================

    /**
     * Validate payment without execution.
     * POST /payments/validate
     */
    @PostMapping("/payments/validate")
    public Mono<ResponseEntity<ValidationResponse>> validatePayment(@RequestBody ValidatePaymentRequest request) {
        log.debug("Validating payment request");
        return railAdapter.payments().validatePayment(request);
    }

    /**
     * Simulate payment execution (fees, dates, etc.).
     * POST /payments/simulate
     */
    @PostMapping("/payments/simulate")
    public Mono<ResponseEntity<SimulationResponse>> simulatePayment(@RequestBody SimulatePaymentRequest request) {
        log.debug("Simulating payment execution");
        return railAdapter.payments().simulatePayment(request);
    }

    // ==================== TWO-PHASE COMMIT WITH SCA ====================

    /**
     * Authorize payment (Phase 1).
     * Reserves funds, may trigger SCA challenge.
     * POST /payments/authorize
     */
    @PostMapping("/payments/authorize")
    public Mono<ResponseEntity<AuthorizationResponse>> authorizePayment(@RequestBody AuthorizePaymentRequest request) {
        log.info("Authorizing payment: amount={}", request.getAmount());
        return railAdapter.payments().authorizePayment(request);
    }

    /**
     * Complete SCA authentication challenge.
     * POST /payments/authenticate
     */
    @PostMapping("/payments/authenticate")
    public Mono<ResponseEntity<AuthorizationResponse>> completeAuthentication(
            @RequestBody CompleteAuthenticationRequest request) {
        log.info("Completing authentication for authorization: {}", request.getAuthorizationId());
        return railAdapter.payments().completeAuthentication(request);
    }

    /**
     * Confirm authorized payment (Phase 2).
     * Actually executes the payment.
     * POST /payments/confirm/{authorizationId}
     */
    @PostMapping("/payments/confirm/{authorizationId}")
    public Mono<ResponseEntity<PaymentResponse>> confirmPayment(@PathVariable String authorizationId) {
        log.info("Confirming payment authorization: {}", authorizationId);
        return railAdapter.payments().confirmPayment(authorizationId);
    }

    /**
     * Cancel authorization before confirmation.
     * DELETE /payments/authorize/{authorizationId}
     */
    @DeleteMapping("/payments/authorize/{authorizationId}")
    public Mono<ResponseEntity<CancellationResponse>> cancelAuthorization(@PathVariable String authorizationId) {
        log.info("Cancelling authorization: {}", authorizationId);
        return railAdapter.payments().cancelAuthorization(authorizationId);
    }

    // ==================== DIRECT EXECUTION (SINGLE PHASE) ====================

    /**
     * Initiate payment directly (single phase).
     * POST /payments
     */
    @PostMapping("/payments")
    public Mono<ResponseEntity<PaymentResponse>> initiatePayment(@RequestBody InitiatePaymentRequest request) {
        log.info("Initiating direct payment: amount={}", request.getAmount());
        return railAdapter.payments().initiatePayment(request);
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get payment by ID.
     * GET /payments/{paymentId}
     */
    @GetMapping("/payments/{paymentId}")
    public Mono<ResponseEntity<PaymentResponse>> getPayment(@PathVariable String paymentId) {
        log.debug("Retrieving payment: {}", paymentId);
        return railAdapter.payments().getPayment(paymentId);
    }

    /**
     * Get payment status by reference.
     * GET /payments/status/{reference}
     */
    @GetMapping("/payments/status/{reference}")
    public Mono<ResponseEntity<PaymentStatusResponse>> getPaymentStatus(@PathVariable String reference) {
        log.debug("Getting payment status: {}", reference);
        return railAdapter.payments().getPaymentStatus(reference);
    }

    /**
     * List payments with filtering.
     * GET /payments
     */
    @GetMapping("/payments")
    public Mono<ResponseEntity<List<PaymentResponse>>> listPayments(ListPaymentsRequest request) {
        log.debug("Listing payments with filters");
        return railAdapter.payments().listPayments(request);
    }

    // ==================== CANCELLATION & RETURNS ====================

    /**
     * Cancel pending payment.
     * DELETE /payments/{paymentId}
     */
    @DeleteMapping("/payments/{paymentId}")
    public Mono<ResponseEntity<PaymentResponse>> cancelPayment(@PathVariable String paymentId) {
        log.info("Cancelling payment: {}", paymentId);
        return railAdapter.payments().cancelPayment(paymentId);
    }

    /**
     * Request payment return/refund.
     * POST /payments/return
     */
    @PostMapping("/payments/return")
    public Mono<ResponseEntity<ReturnResponse>> requestReturn(@RequestBody ReturnRequest request) {
        log.info("Requesting return for payment");
        return railAdapter.payments().requestReturn(request);
    }
}
