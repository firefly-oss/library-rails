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

package com.firefly.rails.dtos.payments;

import com.firefly.rails.domain.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

/**
 * Request DTO for initiating a payment through a banking rail.
 */
@Data
@Builder
public class InitiatePaymentRequest {

    /** Amount and currency */
    @NotNull
    private Money amount;

    /** Originator/debtor account */
    @NotNull
    private BankAccount debtorAccount;

    /** Beneficiary/creditor account */
    @NotNull
    private BankAccount creditorAccount;

    /** End-to-end reference (customer reference) */
    private String endToEndReference;

    /** Payment purpose/remittance information */
    private String remittanceInformation;

    /** Transaction type */
    @NotNull
    private TransactionType transactionType;

    /** Requested settlement speed */
    private SettlementSpeed settlementSpeed;

    /** Requested execution date (for scheduled payments) */
    private LocalDate requestedExecutionDate;

    /** Payment mode (SIMULATION or LIVE) */
    @Builder.Default
    private PaymentMode paymentMode = PaymentMode.LIVE;

    /** Authentication context for SCA compliance */
    private AuthenticationContext authenticationContext;

    /** Whether to request SCA exemption */
    private boolean requestScaExemption;

    /** SCA exemption reason */
    private String scaExemptionReason;

    /** Idempotency key for safe retries */
    private IdempotencyKey idempotencyKey;

    /** Additional metadata */
    private Map<String, String> metadata;
}
