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

/**
 * Service layer for the Firefly Banking Rails Library.
 * 
 * <p>This package contains the abstract service layer that sits between
 * controllers and port implementations, providing cross-cutting concerns:
 * 
 * <ul>
 *   <li><b>Logging</b> - Structured operation logging with rail context</li>
 *   <li><b>Error Handling</b> - Exception mapping and error transformation</li>
 *   <li><b>Resilience</b> - Circuit breaker, retry, rate limiting patterns</li>
 *   <li><b>Metrics</b> - Performance tracking and observability</li>
 *   <li><b>Monitoring</b> - Health checks and status reporting</li>
 * </ul>
 * 
 * <h2>Architecture</h2>
 * <p>The service layer follows the hexagonal architecture pattern:
 * <pre>
 * Controllers → Services → Ports → Adapters → External Systems
 * </pre>
 * 
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link com.firefly.rails.service.AbstractRailService} - Base service with resilience patterns</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @Service
 * public class ACHRailService extends AbstractRailService 
 *         implements PaymentRailPort, SettlementPort {
 *     
 *     private final ACHClient achClient;
 *     
 *     public ACHRailService(ACHClient achClient, MeterRegistry meterRegistry) {
 *         super(RailType.ACH, meterRegistry);
 *         this.achClient = achClient;
 *     }
 *     
 *     @Override
 *     public Mono<ResponseEntity<PaymentResponse>> initiatePayment(
 *             InitiatePaymentRequest request) {
 *         return executeWithResilience("initiatePayment", () -> {
 *             // Implementation with automatic resilience patterns applied
 *             return achClient.initiatePayment(request)
 *                 .map(ResponseEntity::ok);
 *         });
 *     }
 * }
 * }</pre>
 * 
 * @see com.firefly.rails.adapter.ports
 * @see com.firefly.rails.adapter.web
 */
package com.firefly.rails.service;
