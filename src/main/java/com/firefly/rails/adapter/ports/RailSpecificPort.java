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

package com.firefly.rails.adapter.ports;

import com.firefly.rails.dtos.specific.RailOperationRequest;
import com.firefly.rails.dtos.specific.RailOperationResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

/**
 * Port interface for rail-specific operations not covered by standard interfaces.
 * 
 * Allows rail implementations to expose unique features (e.g., SWIFT MT messages,
 * SEPA instant credit transfers, RTP Request for Payment).
 */
public interface RailSpecificPort {

    /**
     * Execute a rail-specific operation.
     *
     * @param operationName the name of the rail-specific operation
     * @param request operation request with parameters
     * @return reactive publisher with operation response
     */
    Mono<ResponseEntity<RailOperationResponse>> executeOperation(String operationName, RailOperationRequest request);

    /**
     * Check if a specific operation is supported by this rail.
     *
     * @param operationName operation name to check
     * @return true if supported, false otherwise
     */
    boolean supportsOperation(String operationName);
}
