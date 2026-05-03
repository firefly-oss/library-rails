/*
 * Copyright 2025 Firefly Software Foundation
 */
package com.firefly.rails.dtos.reconciliation;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ReconciliationReport {
    private String reportId;
    private Instant reportDate;
    private int matchedCount;
    private int unmatchedCount;
    private List<String> discrepancyIds;
}
