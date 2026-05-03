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

package com.firefly.rails.adapter.ports;

import com.firefly.rails.dtos.payments.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Port interface for banking rail payment operations.
 * 
 * Defines the core payment lifecycle operations that all rail implementations must support.
 * Includes validation, simulation, authorization, and confirmation flows.
 */
public interface PaymentRailPort {

    // ==================== VALIDATION & SIMULATION ====================

    /**
     * Validate a payment request without executing it.
     * Performs pre-flight checks: account validation, balance check, format validation, etc.
     *
     * @param request payment validation request
     * @return reactive publisher with validation result
     */
    Mono<ResponseEntity<ValidationResponse>> validatePayment(ValidatePaymentRequest request);

    /**
     * Simulate a payment execution.
     * Returns expected fees, exchange rates, settlement dates without executing.
     *
     * @param request payment simulation request
     * @return reactive publisher with simulation result (fees, timing, etc.)
     */
    Mono<ResponseEntity<SimulationResponse>> simulatePayment(SimulatePaymentRequest request);

    // ==================== AUTHORIZATION (Phase 1) ====================

    /**
     * Authorize a payment (reserve funds, create authorization).
     * First phase of two-phase commit. Does NOT execute the payment.
     * May trigger SCA/authentication flow.
     *
     * @param request payment authorization request
     * @return reactive publisher with authorization response (may include SCA challenge)
     */
    Mono<ResponseEntity<AuthorizationResponse>> authorizePayment(AuthorizePaymentRequest request);

    /**
     * Complete SCA challenge for payment authorization.
     * Used when authorization requires customer authentication.
     *
     * @param request SCA challenge completion request
     * @return reactive publisher with updated authorization response
     */
    Mono<ResponseEntity<AuthorizationResponse>> completeAuthentication(CompleteAuthenticationRequest request);

    // ==================== CONFIRMATION (Phase 2) ====================

    /**
     * Confirm and execute an authorized payment.
     * Second phase of two-phase commit. Actually submits to the rail.
     *
     * @param authorizationId the authorization ID from authorizePayment
     * @return reactive publisher with payment response
     */
    Mono<ResponseEntity<PaymentResponse>> confirmPayment(String authorizationId);

    /**
     * Cancel an authorization before confirmation.
     * Releases reserved funds.
     *
     * @param authorizationId the authorization ID to cancel
     * @return reactive publisher with cancellation response
     */
    Mono<ResponseEntity<CancellationResponse>> cancelAuthorization(String authorizationId);

    // ==================== DIRECT EXECUTION (Single Phase) ====================

    /**
     * Initiate a payment through the rail (direct execution).
     * Single-phase operation that authorizes and executes in one step.
     * Use this for simple flows without SCA or when authorization is not required.
     *
     * @param request payment initiation request
     * @return reactive publisher with payment response
     */
    Mono<ResponseEntity<PaymentResponse>> initiatePayment(InitiatePaymentRequest request);

    // ==================== QUERY OPERATIONS ====================

    /**
     * Retrieve a payment by its identifier.
     *
     * @param paymentId rail-specific payment identifier
     * @return reactive publisher with payment details
     */
    Mono<ResponseEntity<PaymentResponse>> getPayment(String paymentId);

    /**
     * Get payment status by reference.
     *
     * @param paymentReference customer/internal payment reference
     * @return reactive publisher with payment status
     */
    Mono<ResponseEntity<PaymentStatusResponse>> getPaymentStatus(String paymentReference);

    /**
     * List payments with optional filtering.
     *
     * @param request list request with pagination and filters
     * @return reactive publisher with list of payments
     */
    Mono<ResponseEntity<List<PaymentResponse>>> listPayments(ListPaymentsRequest request);

    // ==================== CANCELLATION & RETURNS ====================

    /**
     * Cancel a pending payment.
     * Can only cancel payments that have not yet been settled.
     *
     * @param paymentId payment identifier to cancel
     * @return reactive publisher with cancelled payment response
     */
    Mono<ResponseEntity<PaymentResponse>> cancelPayment(String paymentId);

    /**
     * Request a return/refund of a completed payment.
     *
     * @param request return request with payment ID and reason
     * @return reactive publisher with return response
     */
    Mono<ResponseEntity<ReturnResponse>> requestReturn(ReturnRequest request);
}
