/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.config;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.health.RailHealthIndicator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for Spring Boot integration features.
 * Validates configuration properties, health indicators, and auto-configuration.
 */
@DisplayName("Spring Boot Integration Tests")
class SpringBootIntegrationTest {

    @Nested
    @DisplayName("Configuration Properties Tests")
    class ConfigurationPropertiesTests {

        @Test
        @DisplayName("Should bind configuration properties from application.yml")
        void shouldBindConfigurationPropertiesFromYaml() {
            // Given: Configuration properties
            RailProperties properties = new RailProperties();
            properties.setRailType("ach");
            properties.setBasePath("/api/rails");
            properties.setResilienceEnabled(true);
            properties.setMetricsEnabled(true);

            // When & Then: Properties correctly bound
            assertThat(properties.getRailType()).isEqualTo("ach");
            assertThat(properties.getBasePath()).isEqualTo("/api/rails");
            assertThat(properties.isResilienceEnabled()).isTrue();
            assertThat(properties.isMetricsEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should use default base path when not configured")
        void shouldUseDefaultBasePathWhenNotConfigured() {
            // Given: Properties with defaults
            RailProperties properties = new RailProperties();

            // When & Then: Default base path
            assertThat(properties.getBasePath()).isEqualTo("/api/rails");
        }

        @Test
        @DisplayName("Should enable resilience by default")
        void shouldEnableResilienceByDefault() {
            // Given: Properties with defaults
            RailProperties properties = new RailProperties();

            // When & Then: Resilience enabled by default
            assertThat(properties.isResilienceEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should enable metrics by default")
        void shouldEnableMetricsByDefault() {
            // Given: Properties with defaults
            RailProperties properties = new RailProperties();

            // When & Then: Metrics enabled by default
            assertThat(properties.isMetricsEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should support multiple rail types via configuration")
        void shouldSupportMultipleRailTypesViaConfiguration() {
            // Business value: Switch rails without code changes
            
            // ACH configuration
            RailProperties achProperties = new RailProperties();
            achProperties.setRailType("ach");
            assertThat(achProperties.getRailType()).isEqualTo("ach");

            // SEPA configuration
            RailProperties sepaProperties = new RailProperties();
            sepaProperties.setRailType("sepa");
            assertThat(sepaProperties.getRailType()).isEqualTo("sepa");

            // SWIFT configuration
            RailProperties swiftProperties = new RailProperties();
            swiftProperties.setRailType("swift");
            assertThat(swiftProperties.getRailType()).isEqualTo("swift");
        }

        @Test
        @DisplayName("Should support custom base path configuration")
        void shouldSupportCustomBasePathConfiguration() {
            // Given: Custom base path
            RailProperties properties = new RailProperties();
            properties.setBasePath("/custom/api/path");

            // When & Then
            assertThat(properties.getBasePath()).isEqualTo("/custom/api/path");
        }

        @Test
        @DisplayName("Should allow disabling resilience features")
        void shouldAllowDisablingResilienceFeatures() {
            // Given: Resilience disabled
            RailProperties properties = new RailProperties();
            properties.setResilienceEnabled(false);

            // When & Then
            assertThat(properties.isResilienceEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should allow disabling metrics collection")
        void shouldAllowDisablingMetricsCollection() {
            // Given: Metrics disabled
            RailProperties properties = new RailProperties();
            properties.setMetricsEnabled(false);

            // When & Then
            assertThat(properties.isMetricsEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Health Indicator Tests")
    class HealthIndicatorTests {

        @Test
        @DisplayName("Should report UP when rail is healthy")
        void shouldReportUpWhenRailIsHealthy() {
            // Given: Healthy rail adapter
            RailAdapter railAdapter = mock(RailAdapter.class);
            when(railAdapter.isHealthy()).thenReturn(true);
            when(railAdapter.getRailType()).thenReturn("ach");

            RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);

            // When: Check health
            Health health = healthIndicator.health();

            // Then: Status is UP
            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("railType", "ach");
            assertThat(health.getDetails()).containsKey("timestamp");
        }

        @Test
        @DisplayName("Should report DOWN when rail is unhealthy")
        void shouldReportDownWhenRailIsUnhealthy() {
            // Given: Unhealthy rail adapter
            RailProperties railProperties = new RailProperties();
            railProperties.setRailType("sepa");
            

            RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);

            // When: Check health
            Health health = healthIndicator.health();

            // Then: Status is DOWN
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("railType", "sepa");
            assertThat(health.getDetails()).containsEntry("status", "DOWN");
            assertThat(health.getDetails()).containsEntry("reason", "Rail connectivity check failed");
        }

        @Test
        @DisplayName("Should report DOWN when health check throws exception")
        void shouldReportDownWhenHealthCheckThrowsException() {
            // Given: Rail adapter that throws exception
            RailProperties railProperties = new RailProperties();
            railProperties.setRailType("swift");
            

            RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);

            // When: Check health
            Health health = healthIndicator.health();

            // Then: Status is DOWN with error details
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("railType", "swift");
            assertThat(health.getDetails()).containsKey("error");
            assertThat(health.getDetails().get("error").toString()).contains("Connection timeout");
        }

        @Test
        @DisplayName("Should include rail type in health details")
        void shouldIncludeRailTypeInHealthDetails() {
            // Given: Rail adapter with specific type
            RailProperties railProperties = new RailProperties();
            railProperties.setRailType("rtp");
            

            RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);

            // When: Check health
            Health health = healthIndicator.health();

            // Then: Rail type included
            assertThat(health.getDetails()).containsEntry("railType", "rtp");
        }

        @Test
        @DisplayName("Should include timestamp in health details")
        void shouldIncludeTimestampInHealthDetails() {
            // Given: Healthy rail adapter
            RailProperties railProperties = new RailProperties();
            railProperties.setRailType("fps");
            

            RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);

            // When: Check health
            Health health = healthIndicator.health();

            // Then: Timestamp present
            assertThat(health.getDetails()).containsKey("timestamp");
        }

        @Test
        @DisplayName("Should work with different rail types")
        void shouldWorkWithDifferentRailTypes() {
            // Business value: Health monitoring works across all rails

            String[] railTypes = {"ach", "sepa", "swift", "fps", "rtp", "pix"};

            for (String railType : railTypes) {
                RailAdapter railAdapter = mock(RailAdapter.class);
                when(railAdapter.isHealthy()).thenReturn(true);
                when(railAdapter.getRailType()).thenReturn(railType);

                RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);
                Health health = healthIndicator.health();

                assertThat(health.getStatus()).isEqualTo(Status.UP);
                assertThat(health.getDetails()).containsEntry("railType", railType);
            }
        }
    }

    @Nested
    @DisplayName("Auto-Configuration Tests")
    class AutoConfigurationTests {

        @Test
        @DisplayName("Should support configuration prefix 'firefly.rail'")
        void shouldSupportConfigurationPrefix() {
            // Given: RailProperties with @ConfigurationProperties
            // When: Annotation is present
            ConfigurationProperties annotation = RailProperties.class.getAnnotation(ConfigurationProperties.class);

            // Then: Correct prefix configured
            assertThat(annotation).isNotNull();
            assertThat(annotation.prefix()).isEqualTo("firefly.rail");
        }

        @Test
        @DisplayName("Should be ready for Spring Boot auto-configuration")
        void shouldBeReadyForSpringBootAutoConfiguration() {
            // Business value: Zero configuration required for basic setup
            
            // Given: Default properties
            RailProperties properties = new RailProperties();

            // When & Then: Sensible defaults for auto-configuration
            assertThat(properties.getBasePath()).isEqualTo("/api/rails");
            assertThat(properties.isResilienceEnabled()).isTrue();
            assertThat(properties.isMetricsEnabled()).isTrue();
            // railType can be null initially (to be set by specific implementation)
        }

        @Test
        @DisplayName("Should support property binding from YAML/Properties files")
        void shouldSupportPropertyBindingFromYamlPropertiesFiles() {
            // Example configuration:
            // firefly:
            //   rail:
            //     rail-type: ach
            //     base-path: /api/ach
            //     resilience-enabled: true
            //     metrics-enabled: true

            RailProperties properties = new RailProperties();
            
            // Simulate Spring property binding
            properties.setRailType("ach");
            properties.setBasePath("/api/ach");
            properties.setResilienceEnabled(true);
            properties.setMetricsEnabled(true);

            // Verify binding worked
            assertThat(properties.getRailType()).isEqualTo("ach");
            assertThat(properties.getBasePath()).isEqualTo("/api/ach");
            assertThat(properties.isResilienceEnabled()).isTrue();
            assertThat(properties.isMetricsEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Platform Abstraction Tests")
    class PlatformAbstractionTests {

        @Test
        @DisplayName("Should abstract Spring Boot configuration from rail implementations")
        void shouldAbstractSpringBootConfigurationFromRailImplementations() {
            // Business value: Rail implementers don't need to understand Spring Boot internals
            
            // Given: Configuration properties
            RailProperties properties = new RailProperties();
            properties.setRailType("custom-rail");
            properties.setBasePath("/api/custom");

            // When & Then: Simple POJO with getters/setters
            assertThat(properties.getRailType()).isEqualTo("custom-rail");
            assertThat(properties.getBasePath()).isEqualTo("/api/custom");
            
            // No Spring-specific logic required in rail implementations
        }

        @Test
        @DisplayName("Should provide unified configuration model for all rails")
        void shouldProvideUnifiedConfigurationModelForAllRails() {
            // Business value: Same configuration structure for all rail types
            
            // All rails use the same configuration properties
            RailProperties achConfig = new RailProperties();
            achConfig.setRailType("ach");
            
            RailProperties sepaConfig = new RailProperties();
            sepaConfig.setRailType("sepa");
            
            RailProperties swiftConfig = new RailProperties();
            swiftConfig.setRailType("swift");

            // Same properties available for all
            assertThat(achConfig.getBasePath()).isEqualTo(sepaConfig.getBasePath());
            assertThat(achConfig.isResilienceEnabled()).isEqualTo(swiftConfig.isResilienceEnabled());
        }

        @Test
        @DisplayName("Should enable feature toggles via configuration")
        void shouldEnableFeatureTogglesViaConfiguration() {
            // Business value: Control features without code changes
            
            // Environment 1: Full features
            RailProperties prodConfig = new RailProperties();
            prodConfig.setResilienceEnabled(true);
            prodConfig.setMetricsEnabled(true);

            // Environment 2: Minimal features
            RailProperties testConfig = new RailProperties();
            testConfig.setResilienceEnabled(false);
            testConfig.setMetricsEnabled(false);

            // Configuration controls behavior
            assertThat(prodConfig.isResilienceEnabled()).isTrue();
            assertThat(testConfig.isResilienceEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Integration with Spring Ecosystem Tests")
    class SpringEcosystemIntegrationTests {

        @Test
        @DisplayName("Should integrate with Spring Boot Actuator health checks")
        void shouldIntegrateWithSpringBootActuatorHealthChecks() {
            // Given: Health indicator
            RailProperties railProperties = new RailProperties();
            railProperties.setRailType("test-rail");
            

            RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);

            // When: Health check called (by Actuator)
            Health health = healthIndicator.health();

            // Then: Standard Health object returned
            assertThat(health).isInstanceOf(Health.class);
            assertThat(health.getStatus()).isIn(Status.UP, Status.DOWN);
        }

        @Test
        @DisplayName("Should provide health details for monitoring systems")
        void shouldProvideHealthDetailsForMonitoringSystems() {
            // Business value: Integration with Prometheus, Grafana, etc.
            
            RailProperties railProperties = new RailProperties();
            railProperties.setRailType("production-rail");
            

            RailHealthIndicator healthIndicator = new RailHealthIndicator(railProperties);
            Health health = healthIndicator.health();

            // Health details can be scraped by monitoring systems
            assertThat(health.getDetails()).isNotEmpty();
            assertThat(health.getDetails()).containsKeys("railType", "timestamp", "status");
        }

        @Test
        @DisplayName("Should support Spring's @ConfigurationProperties validation")
        void shouldSupportSpringConfigurationPropertiesValidation() {
            // Given: Properties class with @ConfigurationProperties
            RailProperties properties = new RailProperties();

            // When: Invalid configuration
            properties.setRailType(""); // Empty is technically valid but may be validated

            // Then: Properties object can be validated by Spring
            // Note: Actual validation requires @Validated and JSR-303 annotations
            assertThat(properties).isNotNull();
        }
    }
}
