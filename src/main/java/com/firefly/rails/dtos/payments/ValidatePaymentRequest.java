/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.dtos.payments;

import com.firefly.rails.domain.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * Request to validate a payment before execution.
 */
@Data
@Builder
public class ValidatePaymentRequest {
    
    @NotNull
    private Money amount;
    
    @NotNull
    private BankAccount debtorAccount;
    
    @NotNull
    private BankAccount creditorAccount;
    
    @NotNull
    private TransactionType transactionType;
    
    private SettlementSpeed settlementSpeed;
    
    /** Whether to check account balance */
    private boolean checkBalance;
    
    /** Whether to validate beneficiary account exists */
    private boolean validateBeneficiary;
}
