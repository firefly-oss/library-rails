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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for banking rails.
 */
@Data
@ConfigurationProperties(prefix = "firefly.rail")
public class RailProperties {

    /** Rail type (ach, swift, sepa, fps, rtp, etc.) */
    private String railType;

    /** Base path for REST endpoints */
    private String basePath = "/api/rails";

    /** Enable resilience features */
    private boolean resilienceEnabled = true;

    /** Enable metrics collection */
    private boolean metricsEnabled = true;
}
