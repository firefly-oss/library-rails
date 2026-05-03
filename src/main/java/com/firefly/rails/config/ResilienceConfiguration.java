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

package com.firefly.rails.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j configuration for the rails library.
 * 
 * <p>Provides enterprise-grade resilience patterns:
 * <ul>
 *   <li><b>Circuit Breaker</b> - Prevents cascading failures by opening circuit after threshold failures</li>
 *   <li><b>Rate Limiter</b> - Protects against rail quota exhaustion by limiting request rate</li>
 *   <li><b>Retry with Backoff</b> - Automatically retries failed operations with exponential backoff</li>
 *   <li><b>Bulkhead</b> - Limits concurrent calls to prevent resource exhaustion</li>
 *   <li><b>Timeout</b> - Prevents hanging operations with configurable timeouts</li>
 * </ul>
 * 
 * <p>All patterns can be configured via application properties:
 * <pre>
 * firefly:
 *   rail:
 *     resilience:
 *       circuit-breaker:
 *         failure-rate-threshold: 50
 *         wait-duration-in-open-state: 60000
 *       rate-limiter:
 *         limit-for-period: 100
 *         limit-refresh-period: 1000
 *       retry:
 *         max-attempts: 3
 *         wait-duration: 1000
 * </pre>
 * 
 * @see CircuitBreaker
 * @see RateLimiter
 * @see Retry
 * @see Bulkhead
 * @see TimeLimiter
 */
@Configuration
@EnableConfigurationProperties(RailProperties.class)
@ConditionalOnProperty(prefix = "firefly.rail.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResilienceConfiguration {
    
    private final RailProperties railProperties;
    
    public ResilienceConfiguration(RailProperties railProperties) {
        this.railProperties = railProperties;
    }
    
    /**
     * Creates a CircuitBreaker registry with default configuration.
     * 
     * <p>Circuit breaker opens after 50% failure rate threshold and waits 60 seconds before attempting half-open state.
     * 
     * @return CircuitBreakerRegistry
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50.0f) // Open circuit if 50% of calls fail
            .waitDurationInOpenState(Duration.ofSeconds(60)) // Wait 60s before trying again
            .slidingWindowSize(100) // Consider last 100 calls
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .minimumNumberOfCalls(10) // At least 10 calls before calculating failure rate
            .permittedNumberOfCallsInHalfOpenState(5) // Allow 5 calls in half-open state
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slowCallRateThreshold(100.0f) // All slow calls contribute to opening
            .slowCallDurationThreshold(Duration.ofSeconds(10)) // Calls >10s are slow
            .build();
        
        return CircuitBreakerRegistry.of(config);
    }
    
    /**
     * Creates a default CircuitBreaker instance for rail services.
     * 
     * @param registry The circuit breaker registry
     * @return CircuitBreaker instance
     */
    @Bean
    public CircuitBreaker railServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("rail-service");
    }
    
    /**
     * Creates a RateLimiter registry with default configuration.
     * 
     * <p>Limits to 100 calls per second to protect against quota exhaustion.
     * 
     * @return RateLimiterRegistry
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(100) // 100 calls
            .limitRefreshPeriod(Duration.ofSeconds(1)) // per second
            .timeoutDuration(Duration.ofSeconds(5)) // Wait max 5s for permission
            .build();
        
        return RateLimiterRegistry.of(config);
    }
    
    /**
     * Creates a default RateLimiter instance for rail services.
     * 
     * @param registry The rate limiter registry
     * @return RateLimiter instance
     */
    @Bean
    public RateLimiter railServiceRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("rail-service");
    }
    
    /**
     * Creates a Retry registry with exponential backoff configuration.
     * 
     * <p>Retries up to 3 times with exponential backoff starting at 1 second.
     * 
     * @return RetryRegistry
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3) // Retry up to 3 times
            .waitDuration(Duration.ofSeconds(1)) // Initial wait
            .retryExceptions(Exception.class) // Retry on any exception
            .ignoreExceptions() // Don't retry on these (none for now)
            .failAfterMaxAttempts(true)
            .build();
        
        return RetryRegistry.of(config);
    }
    
    /**
     * Creates a default Retry instance for rail services.
     * 
     * @param registry The retry registry
     * @return Retry instance
     */
    @Bean
    public Retry railServiceRetry(RetryRegistry registry) {
        return registry.retry("rail-service");
    }
    
    /**
     * Creates a Bulkhead registry to limit concurrent operations.
     * 
     * <p>Limits to 25 concurrent operations with a wait queue of 50.
     * 
     * @return BulkheadRegistry
     */
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig config = BulkheadConfig.custom()
            .maxConcurrentCalls(25) // Max 25 concurrent calls
            .maxWaitDuration(Duration.ofSeconds(10)) // Wait max 10s for permission
            .build();
        
        return BulkheadRegistry.of(config);
    }
    
    /**
     * Creates a default Bulkhead instance for rail services.
     * 
     * @param registry The bulkhead registry
     * @return Bulkhead instance
     */
    @Bean
    public Bulkhead railServiceBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("rail-service");
    }
    
    /**
     * Creates a TimeLimiter registry for timeout protection.
     * 
     * <p>Operations timeout after 30 seconds by default.
     * 
     * @return TimeLimiterRegistry
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(30)) // 30 second timeout
            .cancelRunningFuture(true) // Cancel the running task on timeout
            .build();
        
        return TimeLimiterRegistry.of(config);
    }
    
    /**
     * Creates a default TimeLimiter instance for rail services.
     * 
     * @param registry The time limiter registry
     * @return TimeLimiter instance
     */
    @Bean
    public TimeLimiter railServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("rail-service");
    }
}
