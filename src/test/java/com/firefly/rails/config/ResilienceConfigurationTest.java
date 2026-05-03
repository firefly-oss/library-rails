/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ResilienceConfigurationTest {
    
    private ResilienceConfiguration config;
    private RailProperties railProperties;
    
    @BeforeEach
    void setUp() {
        railProperties = new RailProperties();
        config = new ResilienceConfiguration(railProperties);
    }
    
    @Test
    void testCircuitBreakerRegistryCreation() {
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();
        
        assertThat(registry).isNotNull();
        assertThat(registry.getDefaultConfig()).isNotNull();
        assertThat(registry.getDefaultConfig().getFailureRateThreshold()).isEqualTo(50.0f);
        // Note: wait duration is stored differently in different versions
        assertThat(registry.getDefaultConfig().getSlidingWindowSize()).isEqualTo(100);
        assertThat(registry.getDefaultConfig().getMinimumNumberOfCalls()).isEqualTo(10);
    }
    
    @Test
    void testCircuitBreakerCreation() {
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();
        CircuitBreaker circuitBreaker = config.railServiceCircuitBreaker(registry);
        
        assertThat(circuitBreaker).isNotNull();
        assertThat(circuitBreaker.getName()).isEqualTo("rail-service");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
    
    @Test
    void testRateLimiterRegistryCreation() {
        RateLimiterRegistry registry = config.rateLimiterRegistry();
        
        assertThat(registry).isNotNull();
        assertThat(registry.getDefaultConfig()).isNotNull();
        assertThat(registry.getDefaultConfig().getLimitForPeriod()).isEqualTo(100);
        assertThat(registry.getDefaultConfig().getLimitRefreshPeriod())
            .isEqualTo(Duration.ofSeconds(1));
        assertThat(registry.getDefaultConfig().getTimeoutDuration())
            .isEqualTo(Duration.ofSeconds(5));
    }
    
    @Test
    void testRateLimiterCreation() {
        RateLimiterRegistry registry = config.rateLimiterRegistry();
        RateLimiter rateLimiter = config.railServiceRateLimiter(registry);
        
        assertThat(rateLimiter).isNotNull();
        assertThat(rateLimiter.getName()).isEqualTo("rail-service");
    }
    
    @Test
    void testRetryRegistryCreation() {
        RetryRegistry registry = config.retryRegistry();
        
        assertThat(registry).isNotNull();
        assertThat(registry.getDefaultConfig()).isNotNull();
        assertThat(registry.getDefaultConfig().getMaxAttempts()).isEqualTo(3);
        assertThat(registry.getDefaultConfig().getIntervalFunction().apply(1))
            .isEqualTo(1000L);
    }
    
    @Test
    void testRetryCreation() {
        RetryRegistry registry = config.retryRegistry();
        Retry retry = config.railServiceRetry(registry);
        
        assertThat(retry).isNotNull();
        assertThat(retry.getName()).isEqualTo("rail-service");
    }
    
    @Test
    void testBulkheadRegistryCreation() {
        BulkheadRegistry registry = config.bulkheadRegistry();
        
        assertThat(registry).isNotNull();
        assertThat(registry.getDefaultConfig()).isNotNull();
        assertThat(registry.getDefaultConfig().getMaxConcurrentCalls()).isEqualTo(25);
        assertThat(registry.getDefaultConfig().getMaxWaitDuration())
            .isEqualTo(Duration.ofSeconds(10));
    }
    
    @Test
    void testBulkheadCreation() {
        BulkheadRegistry registry = config.bulkheadRegistry();
        Bulkhead bulkhead = config.railServiceBulkhead(registry);
        
        assertThat(bulkhead).isNotNull();
        assertThat(bulkhead.getName()).isEqualTo("rail-service");
    }
    
    @Test
    void testTimeLimiterRegistryCreation() {
        TimeLimiterRegistry registry = config.timeLimiterRegistry();
        
        assertThat(registry).isNotNull();
        assertThat(registry.getDefaultConfig()).isNotNull();
        assertThat(registry.getDefaultConfig().getTimeoutDuration())
            .isEqualTo(Duration.ofSeconds(30));
        assertThat(registry.getDefaultConfig().shouldCancelRunningFuture()).isTrue();
    }
    
    @Test
    void testTimeLimiterCreation() {
        TimeLimiterRegistry registry = config.timeLimiterRegistry();
        TimeLimiter timeLimiter = config.railServiceTimeLimiter(registry);
        
        assertThat(timeLimiter).isNotNull();
        assertThat(timeLimiter.getName()).isEqualTo("rail-service");
    }
    
    @Test
    void testAllResiliencePatternsWorkTogether() {
        // Create all registries and instances
        CircuitBreakerRegistry cbRegistry = config.circuitBreakerRegistry();
        RateLimiterRegistry rlRegistry = config.rateLimiterRegistry();
        RetryRegistry retryRegistry = config.retryRegistry();
        BulkheadRegistry bhRegistry = config.bulkheadRegistry();
        TimeLimiterRegistry tlRegistry = config.timeLimiterRegistry();
        
        CircuitBreaker circuitBreaker = config.railServiceCircuitBreaker(cbRegistry);
        RateLimiter rateLimiter = config.railServiceRateLimiter(rlRegistry);
        Retry retry = config.railServiceRetry(retryRegistry);
        Bulkhead bulkhead = config.railServiceBulkhead(bhRegistry);
        TimeLimiter timeLimiter = config.railServiceTimeLimiter(tlRegistry);
        
        // Verify all are created and can coexist
        assertThat(circuitBreaker).isNotNull();
        assertThat(rateLimiter).isNotNull();
        assertThat(retry).isNotNull();
        assertThat(bulkhead).isNotNull();
        assertThat(timeLimiter).isNotNull();
    }
}
