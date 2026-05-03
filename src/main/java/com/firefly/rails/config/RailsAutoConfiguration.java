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

import com.firefly.rails.health.RailHealthIndicator;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for the Firefly Banking Rails Library.
 * 
 * <p>This auto-configuration class sets up all necessary beans and configuration
 * for the rails library to function properly in a Spring Boot application.
 * 
 * <p>Features configured:
 * <ul>
 *   <li>Rail properties binding</li>
 *   <li>Resilience4j patterns (circuit breaker, retry, rate limiting, etc.)</li>
 *   <li>Health indicators for monitoring</li>
 *   <li>Metrics and observability</li>
 *   <li>Web controllers (if enabled)</li>
 * </ul>
 * 
 * <h2>Configuration Properties</h2>
 * <pre>
 * firefly:
 *   rail:
 *     rail-type: ach          # The type of rail to use (ach, swift, sepa, etc.)
 *     base-path: /api/rails   # Base path for REST endpoints
 *     enabled: true           # Enable/disable the library
 *     resilience:
 *       enabled: true         # Enable/disable resilience patterns
 *     health:
 *       enabled: true         # Enable/disable health checks
 * </pre>
 * 
 * <h2>Usage</h2>
 * <p>Simply add the dependency to your Spring Boot application and the library
 * will auto-configure itself. No additional configuration is needed unless you
 * want to customize the defaults.
 * 
 * <pre>{@code
 * @SpringBootApplication
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }</pre>
 * 
 * @see RailProperties
 * @see ResilienceConfiguration
 * @see RailHealthIndicator
 */
@AutoConfiguration
@EnableConfigurationProperties(RailProperties.class)
@ConditionalOnProperty(prefix = "firefly.rail", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({ResilienceConfiguration.class})
public class RailsAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(RailsAutoConfiguration.class);
    
    private final RailProperties railProperties;
    
    public RailsAutoConfiguration(RailProperties railProperties) {
        this.railProperties = railProperties;
        logger.info("Initializing Firefly Banking Rails Library - Rail Type: {}", 
            railProperties.getRailType());
    }
    
    /**
     * Creates a RailHealthIndicator bean if health checks are enabled and Spring Actuator is on the classpath.
     * 
     * @return RailHealthIndicator bean
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
    @ConditionalOnProperty(prefix = "firefly.rail.health", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public RailHealthIndicator railHealthIndicator() {
        logger.info("Registering RailHealthIndicator for rail type: {}", railProperties.getRailType());
        return new RailHealthIndicator(railProperties);
    }
    
    /**
     * Configuration callback that logs the active configuration on startup.
     */
    @Bean
    public RailsConfigurationLogger railsConfigurationLogger() {
        return new RailsConfigurationLogger(railProperties);
    }
    
    /**
     * Helper class to log configuration on startup.
     */
    static class RailsConfigurationLogger {
        public RailsConfigurationLogger(RailProperties properties) {
            logger.info("╔════════════════════════════════════════════════════════════════╗");
            logger.info("║   Firefly Banking Rails Library - Configuration Summary       ║");
            logger.info("╠════════════════════════════════════════════════════════════════╣");
            logger.info("║ Rail Type:        {}", String.format("%-43s", properties.getRailType()) + "║");
            logger.info("║ Base Path:        {}", String.format("%-43s", properties.getBasePath()) + "║");
            logger.info("║ Resilience:       {}", String.format("%-43s", "Enabled") + "║");
            logger.info("║ Health Checks:    {}", String.format("%-43s", "Enabled") + "║");
            logger.info("║ Metrics:          {}", String.format("%-43s", "Enabled (Micrometer)") + "║");
            logger.info("╚════════════════════════════════════════════════════════════════╝");
        }
    }
}
