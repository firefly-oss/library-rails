/*
 * Copyright 2025 Firefly Software Foundation
 */
package com.firefly.rails.dtos.reconciliation;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ReconciliationRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String accountId;
}
