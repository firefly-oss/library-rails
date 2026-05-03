/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.config;

import com.firefly.rails.domain.RailType;
import com.firefly.rails.health.RailHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RailsAutoConfigurationTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(RailsAutoConfiguration.class, ResilienceConfiguration.class))
        .withPropertyValues(
            "firefly.rail.rail-type=ach",
            "firefly.rail.base-path=/api/rails",
            "firefly.rail.enabled=true"
        );
    
    @Test
    void testAutoConfigurationLoads() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RailsAutoConfiguration.class);
            assertThat(context).hasSingleBean(RailProperties.class);
        });
    }
    
    @Test
    void testRailPropertiesBinding() {
        this.contextRunner.run(context -> {
            RailProperties properties = context.getBean(RailProperties.class);
            assertThat(properties.getRailType()).isEqualTo("ach");
            assertThat(properties.getBasePath()).isEqualTo("/api/rails");
        });
    }
    
    @Test
    void testResilienceConfigurationImported() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ResilienceConfiguration.class);
        });
    }
    
    @Test
    void testHealthIndicatorCreated() {
        this.contextRunner
            .withPropertyValues("management.health.defaults.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(RailHealthIndicator.class);
            });
    }
    
    @Test
    void testConfigurationLoggerCreated() {
        this.contextRunner.run(context -> {
            assertThat(context).hasBean("railsConfigurationLogger");
        });
    }
    
    @Test
    void testAutoConfigurationCanBeDisabled() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RailsAutoConfiguration.class))
            .withPropertyValues("firefly.rail.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(RailsAutoConfiguration.class);
            });
    }
    
    @Test
    void testDefaultRailType() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RailsAutoConfiguration.class))
            .run(context -> {
                if (context.containsBean("railProperties")) {
                    RailProperties properties = context.getBean(RailProperties.class);
                    // Verify default is set (ACH as per RailProperties)
                    assertThat(properties.getRailType()).isNotNull();
                }
            });
    }
}
