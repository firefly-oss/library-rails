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
 * Fedwire specific attributes.
 * Federal Reserve Wire Network for real-time gross settlement in the US.
 */
public class FedwireSpecific {

    @Data
    @Builder
    public static class FedwirePayment {
        /** IMAD (Input Message Accountability Data) */
        private String imad;
        
        /** OMAD (Output Message Accountability Data) */
        private String omad;
        
        /** Type code (1000 - Customer transfer, 1500 - Bank transfer) */
        private String typeCode;
        
        /** Subtype code */
        private String subtypeCode;
        
        /** Originator routing number */
        private String originatorRoutingNumber;
        
        /** Beneficiary routing number */
        private String beneficiaryRoutingNumber;
        
        /** Business function code */
        private BusinessFunctionCode businessFunctionCode;
        
        /** Originator to beneficiary information (up to 4 lines) */
        private String[] originatorToBeneficiaryInfo;
        
        /** Federal Investigative Agencies information */
        private String fiaInfo;
        
        /** Advice information */
        private String adviceInfo;
    }

    public enum BusinessFunctionCode {
        /** Bank transfer */
        BTR,
        
        /** Customer or Corporate Drawdown Request */
        DRC,
        
        /** Customer Transfer */
        CTR,
        
        /** Customer Transfer Plus */
        CTP,
        
        /** Bank-to-Bank Transfer */
        BTB,
        
        /** Service Message */
        SVC
    }
}
