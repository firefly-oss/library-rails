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

package com.firefly.rails.service;

import com.firefly.rails.adapter.ports.*;
import com.firefly.rails.domain.RailType;
import com.firefly.rails.exceptions.RailException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Abstract base service for all rail implementations.
 * 
 * <p>Provides common functionality for all rail services including:
 * <ul>
 *   <li>Structured logging with operation tracking</li>
 *   <li>Error handling and exception mapping</li>
 *   <li>Resilience patterns (circuit breaker, retry, rate limiting)</li>
 *   <li>Metrics collection and observability</li>
 *   <li>Performance monitoring</li>
 * </ul>
 * 
 * <p>Rail implementations should extend this class to inherit all common functionality.
 * The service layer sits between controllers and port implementations, providing
 * cross-cutting concerns.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @Service
 * public class ACHRailService extends AbstractRailService 
 *         implements PaymentRailPort, SettlementPort {
 *     
 *     public ACHRailService(MeterRegistry meterRegistry) {
 *         super(RailType.ACH, meterRegistry);
 *     }
 *     
 *     @Override
 *     public Mono<ResponseEntity<PaymentResponse>> initiatePayment(
 *             InitiatePaymentRequest request) {
 *         return executeWithResilience("initiatePayment", () -> 
 *             doInitiatePayment(request)
 *         );
 *     }
 * }
 * }</pre>
 * 
 * @see PaymentRailPort
 * @see SettlementPort
 * @see StatusPort
 */
public abstract class AbstractRailService {
    
    private final Logger logger;
    private final RailType railType;
    private final MeterRegistry meterRegistry;
    
    /**
     * Constructs an AbstractRailService.
     * 
     * @param railType The type of rail this service handles
     * @param meterRegistry The meter registry for metrics collection
     */
    protected AbstractRailService(RailType railType, MeterRegistry meterRegistry) {
        this.railType = railType;
        this.meterRegistry = meterRegistry;
        this.logger = LoggerFactory.getLogger(getClass());
        
        logger.info("Initializing {} rail service", railType);
    }
    
    /**
     * Gets the rail type this service handles.
     * 
     * @return The rail type
     */
    protected RailType getRailType() {
        return railType;
    }
    
    /**
     * Gets the logger for this service.
     * 
     * @return The logger
     */
    protected Logger getLogger() {
        return logger;
    }
    
    /**
     * Gets the meter registry for metrics.
     * 
     * @return The meter registry
     */
    protected MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
    
    /**
     * Executes an operation with full resilience patterns applied.
     * 
     * <p>This method applies:
     * <ul>
     *   <li>Circuit breaker - Prevents cascading failures</li>
     *   <li>Rate limiter - Protects against quota exhaustion</li>
     *   <li>Retry with exponential backoff - Handles transient failures</li>
     *   <li>Bulkhead - Limits concurrent operations</li>
     *   <li>Timeout - Prevents hanging operations</li>
     *   <li>Logging - Structured operation logging</li>
     *   <li>Metrics - Performance and success/failure tracking</li>
     * </ul>
     * 
     * @param <T> The return type
     * @param operationName The name of the operation for logging/metrics
     * @param operation The operation to execute
     * @return A Mono containing the result
     */
    @CircuitBreaker(name = "rail-service", fallbackMethod = "fallbackMono")
    @RateLimiter(name = "rail-service")
    @Retry(name = "rail-service")
    @Bulkhead(name = "rail-service", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "rail-service")
    protected <T> Mono<T> executeWithResilience(String operationName, Supplier<Mono<T>> operation) {
        logger.debug("Starting operation: {} for rail: {}", operationName, railType);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return operation.get()
            .doOnSuccess(result -> {
                sample.stop(meterRegistry.timer("rail.operation", 
                    "rail", railType.name(),
                    "operation", operationName,
                    "status", "success"));
                logger.debug("Operation completed successfully: {} for rail: {}", operationName, railType);
                
                // Increment success counter
                meterRegistry.counter("rail.operation.success",
                    "rail", railType.name(),
                    "operation", operationName).increment();
            })
            .doOnError(error -> {
                sample.stop(meterRegistry.timer("rail.operation",
                    "rail", railType.name(),
                    "operation", operationName,
                    "status", "error"));
                logger.error("Operation failed: {} for rail: {} - Error: {}", 
                    operationName, railType, error.getMessage(), error);
                
                // Increment error counter
                meterRegistry.counter("rail.operation.error",
                    "rail", railType.name(),
                    "operation", operationName,
                    "error", error.getClass().getSimpleName()).increment();
            })
            .onErrorMap(this::mapException);
    }
    
    /**
     * Executes a Flux operation with resilience patterns.
     * 
     * @param <T> The element type
     * @param operationName The name of the operation
     * @param operation The operation to execute
     * @return A Flux containing the results
     */
    @CircuitBreaker(name = "rail-service", fallbackMethod = "fallbackFlux")
    @RateLimiter(name = "rail-service")
    @Retry(name = "rail-service")
    @Bulkhead(name = "rail-service", type = Bulkhead.Type.SEMAPHORE)
    protected <T> Flux<T> executeFluxWithResilience(String operationName, Supplier<Flux<T>> operation) {
        logger.debug("Starting flux operation: {} for rail: {}", operationName, railType);
        
        return operation.get()
            .doOnComplete(() -> {
                logger.debug("Flux operation completed: {} for rail: {}", operationName, railType);
                meterRegistry.counter("rail.operation.success",
                    "rail", railType.name(),
                    "operation", operationName).increment();
            })
            .doOnError(error -> {
                logger.error("Flux operation failed: {} for rail: {} - Error: {}", 
                    operationName, railType, error.getMessage(), error);
                meterRegistry.counter("rail.operation.error",
                    "rail", railType.name(),
                    "operation", operationName,
                    "error", error.getClass().getSimpleName()).increment();
            })
            .onErrorMap(this::mapException);
    }
    
    /**
     * Maps exceptions to appropriate RailException subclasses.
     * 
     * <p>Override this method to provide custom exception mapping for specific rails.
     * 
     * @param throwable The original exception
     * @return The mapped exception
     */
    protected Throwable mapException(Throwable throwable) {
        if (throwable instanceof RailException) {
            return throwable;
        }
        
        // Default mapping
        logger.warn("Unmapped exception type: {}, wrapping in RailException", 
            throwable.getClass().getName());
        return new RailException("Rail operation failed: " + throwable.getMessage(), throwable);
    }
    
    /**
     * Fallback method for Mono operations when circuit breaker is open.
     * 
     * @param <T> The return type
     * @param operationName The operation name
     * @param operation The original operation (unused in fallback)
     * @param throwable The exception that triggered the fallback
     * @return A Mono containing the fallback response
     */
    protected <T> Mono<T> fallbackMono(String operationName, Supplier<Mono<T>> operation, Throwable throwable) {
        logger.warn("Circuit breaker fallback triggered for operation: {} on rail: {} - Reason: {}", 
            operationName, railType, throwable.getMessage());
        
        meterRegistry.counter("rail.operation.fallback",
            "rail", railType.name(),
            "operation", operationName).increment();
        
        return Mono.error(new RailException(
            String.format("Service temporarily unavailable for %s rail. Please try again later.", railType),
            throwable
        ));
    }
    
    /**
     * Fallback method for Flux operations when circuit breaker is open.
     * 
     * @param <T> The element type
     * @param operationName The operation name
     * @param operation The original operation (unused in fallback)
     * @param throwable The exception that triggered the fallback
     * @return A Flux containing the fallback response
     */
    protected <T> Flux<T> fallbackFlux(String operationName, Supplier<Flux<T>> operation, Throwable throwable) {
        logger.warn("Circuit breaker fallback triggered for flux operation: {} on rail: {} - Reason: {}", 
            operationName, railType, throwable.getMessage());
        
        meterRegistry.counter("rail.operation.fallback",
            "rail", railType.name(),
            "operation", operationName).increment();
        
        return Flux.error(new RailException(
            String.format("Service temporarily unavailable for %s rail. Please try again later.", railType),
            throwable
        ));
    }
    
    /**
     * Records a custom metric.
     * 
     * @param metricName The metric name
     * @param value The metric value
     * @param tags Additional tags (key-value pairs)
     */
    protected void recordMetric(String metricName, double value, String... tags) {
        String[] allTags = new String[tags.length + 2];
        allTags[0] = "rail";
        allTags[1] = railType.name();
        System.arraycopy(tags, 0, allTags, 2, tags.length);
        
        // Convert tags to Tag objects
        java.util.List<io.micrometer.core.instrument.Tag> tagList = new java.util.ArrayList<>();
        for (int i = 0; i < allTags.length; i += 2) {
            if (i + 1 < allTags.length) {
                tagList.add(io.micrometer.core.instrument.Tag.of(allTags[i], allTags[i + 1]));
            }
        }
        
        meterRegistry.gauge(metricName, tagList, value);
    }
    
    /**
     * Logs an operation with structured data.
     * 
     * @param level The log level (INFO, WARN, ERROR)
     * @param operation The operation name
     * @param message The message
     * @param args Additional arguments for the message
     */
    protected void logOperation(String level, String operation, String message, Object... args) {
        String formattedMessage = String.format("[%s] [%s] %s", railType, operation, message);
        
        switch (level.toUpperCase()) {
            case "INFO" -> logger.info(formattedMessage, args);
            case "WARN" -> logger.warn(formattedMessage, args);
            case "ERROR" -> logger.error(formattedMessage, args);
            case "DEBUG" -> logger.debug(formattedMessage, args);
            default -> logger.info(formattedMessage, args);
        }
    }
}
