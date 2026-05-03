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

package com.firefly.rails.domain.railspecific;

import lombok.Builder;
import lombok.Data;

/** 
 * BACS specific attributes.
 * UK Bankers' Automated Clearing Services for bulk payment processing.
 */
public class BACSSpecific {

    @Data
    @Builder
    public static class BACSPayment {
        /** BACS Service User Number (SUN) */
        private String serviceUserNumber;
        
        /** Transaction code (Direct Credit, Direct Debit) */
        private TransactionCode transactionCode;
        
        /** Sort code */
        private String sortCode;
        
        /** Account number */
        private String accountNumber;
        
        /** Originating account name */
        private String originatingAccountName;
        
        /** Reference (up to 18 characters) */
        private String reference;
        
        /** Processing date */
        private String processingDate;
        
        /** Direct Debit specific information */
        private DirectDebitInfo directDebitInfo;
    }

    @Data
    @Builder
    public static class DirectDebitInfo {
        /** Direct Debit Instruction (DDI) reference */
        private String ddiReference;
        
        /** Advance notice given (in days) */
        private Integer advanceNoticeDays;
        
        /** Indemnity claim period */
        private IndemnityClaimPeriod indemnityClaimPeriod;
    }

    public enum TransactionCode {
        /** Standard direct credit (99) */
        DIRECT_CREDIT_99,
        
        /** Standard direct debit (17) */
        DIRECT_DEBIT_17,
        
        /** Direct debit first payment (18) */
        DIRECT_DEBIT_FIRST_18,
        
        /** Direct debit re-presented (19) */
        DIRECT_DEBIT_REPRESENTED_19,
        
        /** Dividend payment (01) */
        DIVIDEND_01
    }

    public enum IndemnityClaimPeriod {
        /** Immediate claim (within 2 months) */
        IMMEDIATE,
        
        /** Extended claim (within 13 months) */
        EXTENDED
    }
}
