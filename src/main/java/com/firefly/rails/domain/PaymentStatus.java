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

package com.firefly.rails.domain;

/**
 * Status of a payment transaction in a banking rail.
 */
public enum PaymentStatus {
    
    /** Payment has been initiated but not yet submitted to the rail */
    INITIATED,
    
    /** Payment is pending validation */
    PENDING_VALIDATION,
    
    /** Payment has been submitted to the rail */
    SUBMITTED,
    
    /** Payment is being processed by the rail */
    PROCESSING,
    
    /** Payment is pending at intermediary bank */
    PENDING_INTERMEDIARY,
    
    /** Payment has been settled */
    SETTLED,
    
    /** Payment has been completed successfully */
    COMPLETED,
    
    /** Payment has failed */
    FAILED,
    
    /** Payment has been rejected by the rail or beneficiary bank */
    REJECTED,
    
    /** Payment has been returned (e.g., account not found) */
    RETURNED,
    
    /** Payment has been cancelled by the sender */
    CANCELLED,
    
    /** Payment is on hold for compliance review */
    COMPLIANCE_HOLD,
    
    /** Payment is pending reversal */
    PENDING_REVERSAL,
    
    /** Payment has been reversed */
    REVERSED
}
