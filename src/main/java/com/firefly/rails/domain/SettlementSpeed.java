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
 * Settlement speed for banking rail transactions.
 */
public enum SettlementSpeed {
    
    /** Real-time settlement (seconds) */
    INSTANT,
    
    /** Same-day settlement */
    SAME_DAY,
    
    /** Next business day settlement */
    NEXT_DAY,
    
    /** Standard settlement (2-3 business days) */
    STANDARD,
    
    /** Batch processing (variable timing) */
    BATCH
}
