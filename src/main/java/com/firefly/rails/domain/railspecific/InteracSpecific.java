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
 * Interac e-Transfer specific attributes.
 * Canadian electronic funds transfer system.
 */
public class InteracSpecific {

    @Data
    @Builder
    public static class InteracPayment {
        /** Security question (for email money transfer) */
        private String securityQuestion;
        
        /** Security answer hash */
        private String securityAnswerHash;
        
        /** Recipient email or mobile number */
        private String recipientContact;
        
        /** Contact type */
        private ContactType contactType;
        
        /** Sender name */
        private String senderName;
        
        /** Message to recipient */
        private String messageToRecipient;
        
        /** Auto-deposit enabled */
        private boolean autoDepositEnabled;
        
        /** Request Money feature */
        private RequestMoneyInfo requestMoneyInfo;
        
        /** Expiry date for the transfer */
        private String expiryDate;
    }

    @Data
    @Builder
    public static class RequestMoneyInfo {
        /** Is this a money request (vs send) */
        private boolean isRequest;
        
        /** Request ID */
        private String requestId;
        
        /** Request expiry date */
        private String requestExpiryDate;
    }

    public enum ContactType {
        /** Email address */
        EMAIL,
        
        /** Mobile phone number */
        MOBILE
    }
}
