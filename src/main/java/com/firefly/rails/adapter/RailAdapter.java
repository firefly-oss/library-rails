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

package com.firefly.rails.adapter;

import com.firefly.rails.adapter.ports.*;

/**
 * Main Banking Rail adapter interface.
 * 
 * This is the primary port in the hexagonal architecture that defines
 * the standardized contract for all banking rail implementations
 * (ACH, SWIFT, SEPA, FPS, RTP, etc.).
 * 
 * Each implementation should provide access to specific operation ports
 * that handle different aspects of banking rail operations.
 */
public interface RailAdapter {

    /**
     * Get the payment operations port.
     * Handles payment initiation, status queries, and cancellations.
     *
     * @return PaymentRailPort instance
     */
    PaymentRailPort payments();

    /**
     * Get the settlement operations port.
     * Handles settlement reporting and reconciliation.
     *
     * @return SettlementPort instance
     */
    SettlementPort settlement();

    /**
     * Get the status inquiry port.
     * Handles real-time payment status queries and tracking.
     *
     * @return StatusPort instance
     */
    StatusPort status();

    /**
     * Get the mandate management port (for direct debits).
     * Handles direct debit mandate creation, updates, and cancellations.
     *
     * @return MandatePort instance
     */
    MandatePort mandates();

    /**
     * Get the bulk payment port.
     * Handles batch/bulk payment submissions.
     *
     * @return BulkPaymentPort instance
     */
    BulkPaymentPort bulkPayments();

    /**
     * Get the reconciliation port.
     * Handles transaction reconciliation and discrepancy detection.
     *
     * @return ReconciliationPort instance
     */
    ReconciliationPort reconciliation();

    /**
     * Get the rail-specific operations port.
     * Allows access to rail-specific features not covered by standard ports.
     *
     * @return RailSpecificPort instance
     */
    RailSpecificPort railSpecific();

    /**
     * Get the scheduled payment port.
     * Handles future-dated and recurring payments.
     *
     * @return ScheduledPaymentPort instance
     */
    ScheduledPaymentPort scheduledPayments();

    /**
     * Get the compliance port.
     * Handles AML/KYC checks, sanctions screening, and regulatory compliance.
     *
     * @return CompliancePort instance
     */
    CompliancePort compliance();

    /**
     * Get the rail type/provider name.
     *
     * @return rail identifier (e.g., "ach", "swift", "sepa", "fps")
     */
    String getRailType();

    /**
     * Health check to verify rail connectivity and credentials.
     *
     * @return true if rail is reachable and authenticated, false otherwise
     */
    boolean isHealthy();
}
