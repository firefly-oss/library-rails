/*
 * Copyright 2025 Firefly Software Foundation
 */
package com.firefly.rails.dtos.reconciliation;

import lombok.Builder;
import lombok.Data;
import com.firefly.rails.domain.Money;

@Data
@Builder
public class ReconciliationSummary {
    private int totalTransactions;
    private int reconciledTransactions;
    private int pendingTransactions;
    private Money totalAmount;
}
