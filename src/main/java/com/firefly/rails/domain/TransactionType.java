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
 * Type of transaction supported by banking rails.
 */
public enum TransactionType {
    
    /** Credit transfer - pushing funds to another account */
    CREDIT_TRANSFER,
    
    /** Debit transfer - pulling funds from another account */
    DEBIT_TRANSFER,
    
    /** Direct debit - recurring pull authorization */
    DIRECT_DEBIT,
    
    /** Wire transfer - typically for larger amounts */
    WIRE_TRANSFER,
    
    /** Card payment authorization */
    CARD_AUTHORIZATION,
    
    /** Card payment capture */
    CARD_CAPTURE,
    
    /** Card refund */
    CARD_REFUND,
    
    /** Card chargeback */
    CARD_CHARGEBACK,
    
    /** Request to pay - invoice-based payment request */
    REQUEST_TO_PAY,
    
    /** Peer-to-peer transfer */
    P2P_TRANSFER,
    
    /** Bulk payment - multiple beneficiaries */
    BULK_PAYMENT,
    
    /** Standing order - recurring scheduled payment */
    STANDING_ORDER,
    
    /** Reversal of a previous transaction */
    REVERSAL,
    
    /** Return of a failed or rejected payment */
    RETURN
}
