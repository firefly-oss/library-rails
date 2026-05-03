/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Port interfaces defining standardized banking rail operations.
 * 
 * <p>This package contains 9 port interfaces that define all operations
 * supported by banking rails. Each port represents a specific domain of
 * banking operations, following the hexagonal architecture pattern.
 * 
 * <h2>Port Interfaces</h2>
 * <ul>
 *   <li>{@link com.firefly.rails.adapter.ports.PaymentRailPort} - Payment initiation and management (13 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.SettlementPort} - Settlement reporting (4 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.StatusPort} - Real-time status tracking (3 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.MandatePort} - Direct debit mandates (5 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.BulkPaymentPort} - Batch payments (3 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.ReconciliationPort} - Transaction reconciliation (3 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.RailSpecificPort} - Custom rail operations (2 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.ScheduledPaymentPort} - Scheduled & recurring payments (9 methods)</li>
 *   <li>{@link com.firefly.rails.adapter.ports.CompliancePort} - AML/KYC/Sanctions (6 methods)</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Reactive</strong> - All methods return {@code Mono<ResponseEntity<T>>}</li>
 *   <li><strong>Type-Safe</strong> - Strong typing with domain models and DTOs</li>
 *   <li><strong>Rail-Agnostic</strong> - Same interface for all banking rails</li>
 *   <li><strong>Comprehensive</strong> - Covers all common banking operations</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * @Service
 * public class PaymentService {
 *     private final RailAdapter railAdapter;
 *     
 *     public Mono<PaymentResponse> processPayment(PaymentRequest request) {
 *         return railAdapter.payments()
 *             .initiatePayment(request)
 *             .map(ResponseEntity::getBody);
 *     }
 * }
 * }</pre>
 * 
 * @see com.firefly.rails.adapter.RailAdapter
 * @since 1.0.0
 */
package com.firefly.rails.adapter.ports;
