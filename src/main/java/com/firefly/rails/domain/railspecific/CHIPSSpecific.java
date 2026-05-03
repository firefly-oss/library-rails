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
 * CHIPS (Clearing House Interbank Payments System) specific attributes.
 * US large-value payment system operated by The Clearing House.
 */
public class CHIPSSpecific {

    @Data
    @Builder
    public static class CHIPSPayment {
        /** CHIPS Universal Identifier (UID) */
        private String universalIdentifier;
        
        /** CHIPS Participant Number */
        private String participantNumber;
        
        /** Settlement method (Prefunded, Balanced) */
        private SettlementMethod settlementMethod;
        
        /** Type code (Standard, Express) */
        private TypeCode typeCode;
        
        /** Subtype code */
        private String subtypeCode;
        
        /** Remittance information */
        private String remittanceInfo;
        
        /** Reference for beneficiary */
        private String beneficiaryReference;
        
        /** Originator to beneficiary information */
        private String originatorToBeneficiaryInfo;
    }

    public enum SettlementMethod {
        /** Pre-funded through Federal Reserve account */
        PREFUNDED,
        
        /** Balanced settlement */
        BALANCED
    }

    public enum TypeCode {
        /** Standard priority payment */
        STANDARD,
        
        /** Express priority payment */
        EXPRESS
    }
}
