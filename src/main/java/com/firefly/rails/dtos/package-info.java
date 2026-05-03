/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Data Transfer Objects for banking rail operations.
 * 
 * <p>This package contains DTOs organized by functional area. All DTOs use
 * Lombok builders for fluent construction and are designed for JSON serialization.
 * 
 * <h2>DTO Packages</h2>
 * <ul>
 *   <li>{@code payments} - Payment initiation, authorization, and management DTOs</li>
 *   <li>{@code status} - Payment status and tracking DTOs</li>
 *   <li>{@code settlement} - Settlement reporting and reconciliation DTOs</li>
 *   <li>{@code mandate} - Direct debit mandate management DTOs</li>
 *   <li>{@code bulk} - Bulk/batch payment DTOs</li>
 *   <li>{@code scheduled} - Scheduled and recurring payment DTOs</li>
 *   <li>{@code compliance} - AML/KYC compliance DTOs</li>
 *   <li>{@code reconciliation} - Transaction reconciliation DTOs</li>
 *   <li>{@code specific} - Rail-specific operation DTOs</li>
 *   <li>{@code fees} - Fee calculation DTOs</li>
 * </ul>
 * 
 * <h2>Design Pattern</h2>
 * <p>All DTOs follow a consistent pattern:
 * <ul>
 *   <li><strong>Immutable</strong> - Built using Lombok {@code @Builder}</li>
 *   <li><strong>Validated</strong> - Use Jakarta Validation annotations</li>
 *   <li><strong>JSON-Friendly</strong> - Compatible with Jackson serialization</li>
 *   <li><strong>Type-Safe</strong> - Reference domain models where appropriate</li>
 * </ul>
 * 
 * <h2>Example</h2>
 * <pre>{@code
 * InitiatePaymentRequest request = InitiatePaymentRequest.builder()
 *     .amount(new Money(new BigDecimal("1000.00"), Currency.USD))
 *     .debtorAccount(debtorAccount)
 *     .creditorAccount(creditorAccount)
 *     .transactionType(TransactionType.CREDIT_TRANSFER)
 *     .build();
 * }</pre>
 * 
 * @see com.firefly.rails.domain
 * @since 1.0.0
 */
package com.firefly.rails.dtos;
