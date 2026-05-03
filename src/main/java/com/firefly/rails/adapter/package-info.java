/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */

/**
 * Banking Rail adapters implementing the hexagonal architecture pattern.
 * 
 * <p>This package contains the primary adapter interface ({@link com.firefly.rails.adapter.RailAdapter})
 * that defines the standardized contract for all banking rail implementations.
 * 
 * <p>Each banking rail (ACH, SWIFT, SEPA, FPS, RTP, etc.) should implement the
 * {@code RailAdapter} interface to provide rail-specific functionality while
 * maintaining a consistent API across all rails.
 * 
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link com.firefly.rails.adapter.RailAdapter} - Main adapter interface</li>
 *   <li>{@link com.firefly.rails.adapter.ports} - Port interfaces defining operations</li>
 *   <li>{@link com.firefly.rails.adapter.web} - Abstract REST controllers</li>
 * </ul>
 * 
 * <h2>Example Implementation</h2>
 * <pre>{@code
 * @Component
 * public class ACHRailAdapter implements RailAdapter {
 *     
 *     @Override
 *     public PaymentRailPort payments() {
 *         return achPaymentPort;
 *     }
 *     
 *     @Override
 *     public String getRailType() {
 *         return "ach";
 *     }
 *     
 *     // ... implement other ports
 * }
 * }</pre>
 * 
 * @see com.firefly.rails.adapter.RailAdapter
 * @see com.firefly.rails.adapter.ports
 * @since 1.0.0
 */
package com.firefly.rails.adapter;
