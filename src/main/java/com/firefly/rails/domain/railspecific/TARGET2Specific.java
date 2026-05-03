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
 * TARGET2 specific attributes.
 * Trans-European Automated Real-time Gross settlement Express Transfer system.
 */
public class TARGET2Specific {

    @Data
    @Builder
    public static class TARGET2Payment {
        /** BIC (Bank Identifier Code) of the sender */
        private String senderBIC;
        
        /** BIC of the receiver */
        private String receiverBIC;
        
        /** Payment priority */
        private PaymentPriority priority;
        
        /** Service level code */
        private String serviceLevelCode;
        
        /** Transaction Reference Number (TRN) */
        private String transactionReferenceNumber;
        
        /** Related reference */
        private String relatedReference;
        
        /** Instruction for creditor agent */
        private String instructionForCreditorAgent;
        
        /** Purpose code */
        private String purposeCode;
        
        /** Regulatory reporting information */
        private String regulatoryReporting;
        
        /** Remittance information (structured or unstructured) */
        private RemittanceInformation remittanceInformation;
    }

    @Data
    @Builder
    public static class RemittanceInformation {
        private boolean structured;
        private String content;
        private String creditorReferenceType;
        private String creditorReference;
    }

    public enum PaymentPriority {
        /** Normal priority */
        NORM,
        
        /** High priority */
        HIGH,
        
        /** Urgent priority */
        URGP
    }
}
