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

import com.firefly.rails.dtos.reconciliation.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

/**
 * Port interface for transaction reconciliation.
 */
public interface ReconciliationPort {

    Mono<ResponseEntity<ReconciliationReport>> getReconciliationReport(LocalDate date);

    Mono<ResponseEntity<List<DiscrepancyResponse>>> findDiscrepancies(ReconciliationRequest request);

    Mono<ResponseEntity<ReconciliationSummary>> getReconciliationSummary(LocalDate startDate, LocalDate endDate);
}
