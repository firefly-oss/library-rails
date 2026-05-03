/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.adapter.ports;

import com.firefly.rails.dtos.scheduled.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Port interface for scheduled and recurring payment operations.
 * 
 * Handles future-dated payments, standing orders, and recurring payment schedules.
 */
public interface ScheduledPaymentPort {

    /**
     * Create a scheduled payment for future execution.
     *
     * @param request scheduled payment request
     * @return reactive publisher with scheduled payment response
     */
    Mono<ResponseEntity<ScheduledPaymentResponse>> createScheduledPayment(CreateScheduledPaymentRequest request);

    /**
     * Get details of a scheduled payment.
     *
     * @param scheduledPaymentId scheduled payment identifier
     * @return reactive publisher with scheduled payment details
     */
    Mono<ResponseEntity<ScheduledPaymentResponse>> getScheduledPayment(String scheduledPaymentId);

    /**
     * List scheduled payments with filtering.
     *
     * @param request list request with filters
     * @return reactive publisher with list of scheduled payments
     */
    Mono<ResponseEntity<List<ScheduledPaymentResponse>>> listScheduledPayments(ListScheduledPaymentsRequest request);

    /**
     * Cancel a scheduled payment before execution.
     *
     * @param scheduledPaymentId payment to cancel
     * @return reactive publisher with cancellation response
     */
    Mono<ResponseEntity<CancellationResponse>> cancelScheduledPayment(String scheduledPaymentId);

    /**
     * Update a scheduled payment (change date, amount, etc.).
     *
     * @param request update request
     * @return reactive publisher with updated payment
     */
    Mono<ResponseEntity<ScheduledPaymentResponse>> updateScheduledPayment(UpdateScheduledPaymentRequest request);

    /**
     * Create a recurring payment schedule (standing order).
     *
     * @param request recurring payment request
     * @return reactive publisher with recurring payment response
     */
    Mono<ResponseEntity<RecurringPaymentResponse>> createRecurringPayment(CreateRecurringPaymentRequest request);

    /**
     * Get details of a recurring payment.
     *
     * @param recurringPaymentId recurring payment identifier
     * @return reactive publisher with recurring payment details
     */
    Mono<ResponseEntity<RecurringPaymentResponse>> getRecurringPayment(String recurringPaymentId);

    /**
     * Cancel a recurring payment schedule.
     *
     * @param recurringPaymentId payment to cancel
     * @return reactive publisher with cancellation response
     */
    Mono<ResponseEntity<CancellationResponse>> cancelRecurringPayment(String recurringPaymentId);

    /**
     * Get execution history for a recurring payment.
     *
     * @param recurringPaymentId recurring payment identifier
     * @return reactive publisher with execution history
     */
    Mono<ResponseEntity<List<PaymentExecutionHistory>>> getRecurringPaymentHistory(String recurringPaymentId);
}
