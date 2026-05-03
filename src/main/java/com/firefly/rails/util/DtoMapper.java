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

package com.firefly.rails.util;

/**
 * Utility class for DTO mapping operations.
 * 
 * <p>This class provides helper methods for converting between DTOs and domain models.
 * Rail implementations should extend this class or create their own mappers following
 * similar patterns.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class ACHDtoMapper extends DtoMapper {
 *     
 *     public ACHPaymentRequest toACHRequest(InitiatePaymentRequest request) {
 *         return ACHPaymentRequest.builder()
 *             .amount(request.getAmount())
 *             .debtorAccount(request.getDebtorAccount())
 *             .creditorAccount(request.getCreditorAccount())
 *             .build();
 *     }
 *     
 *     public PaymentResponse fromACHResponse(ACHPaymentResponse response) {
 *         return PaymentResponse.builder()
 *             .paymentId(response.getTransactionId())
 *             .status(mapStatus(response.getStatus()))
 *             .build();
 *     }
 * }
 * }</pre>
 * 
 * @see com.firefly.rails.dtos.payments
 * @see com.firefly.rails.domain
 */
public abstract class DtoMapper {
    
    /**
     * Protected constructor to prevent direct instantiation.
     * Subclasses should be created for specific rail implementations.
     */
    protected DtoMapper() {
        // For extension only
    }
}
