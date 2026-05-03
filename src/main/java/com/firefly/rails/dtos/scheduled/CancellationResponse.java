/*
 * Copyright 2025 Firefly Software Foundation
 */
package com.firefly.rails.dtos.scheduled;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

/**
 * Response for payment cancellation operations.
 */
@Data
@Builder
public class CancellationResponse {
    private String paymentId;
    private String status;
    private Instant cancelledAt;
    private String reason;
}
