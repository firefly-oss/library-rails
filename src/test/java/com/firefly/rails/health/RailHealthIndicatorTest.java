/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.health;

import com.firefly.rails.config.RailProperties;
import com.firefly.rails.domain.RailType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class RailHealthIndicatorTest {
    
    private RailHealthIndicator healthIndicator;
    private RailProperties railProperties;
    
    @BeforeEach
    void setUp() {
        railProperties = new RailProperties();
        railProperties.setRailType("ACH");
        railProperties.setBasePath("/api/rails");
        
        healthIndicator = new RailHealthIndicator(railProperties);
    }
    
    @Test
    void testHealthUp() {
        Health health = healthIndicator.health();
        
        assertThat(health).isNotNull();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("railType", "ACH");
        assertThat(health.getDetails()).containsEntry("basePath", "/api/rails");
        assertThat(health.getDetails()).containsKey("timestamp");
        assertThat(health.getDetails()).containsEntry("libraryVersion", "1.0.0-SNAPSHOT");
    }
    
    @Test
    void testHealthWithDifferentRailType() {
        railProperties.setRailType("SWIFT");
        railProperties.setBasePath("/api/swift");
        
        healthIndicator = new RailHealthIndicator(railProperties);
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("railType", "SWIFT");
        assertThat(health.getDetails()).containsEntry("basePath", "/api/swift");
    }
    
    @Test
    void testHealthIndicatorName() {
        // The health indicator should be named "rail" in Spring Actuator
        // This test verifies the indicator is properly constructed
        assertThat(healthIndicator).isNotNull();
    }
}
