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

import com.firefly.rails.dtos.settlement.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

/**
 * Port interface for settlement operations.
 * 
 * Handles settlement reporting and reconciliation for rail transactions.
 */
public interface SettlementPort {

    /**
     * Get settlement report for a specific date.
     *
     * @param date settlement date
     * @return reactive publisher with settlement report
     */
    Mono<ResponseEntity<SettlementReport>> getSettlementReport(LocalDate date);

    /**
     * Get settlement report for a date range.
     *
     * @param request settlement report request with date range
     * @return reactive publisher with list of settlement reports
     */
    Mono<ResponseEntity<List<SettlementReport>>> getSettlementReports(SettlementReportRequest request);

    /**
     * Get detailed settlement transactions.
     *
     * @param settlementId settlement identifier
     * @return reactive publisher with settlement details
     */
    Mono<ResponseEntity<SettlementDetails>> getSettlementDetails(String settlementId);

    /**
     * Query expected settlement date for a payment.
     *
     * @param paymentId payment identifier
     * @return reactive publisher with expected settlement date
     */
    Mono<ResponseEntity<SettlementDateResponse>> getExpectedSettlementDate(String paymentId);
}
