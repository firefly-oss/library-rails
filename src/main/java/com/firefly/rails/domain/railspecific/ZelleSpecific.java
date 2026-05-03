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
 * Zelle specific attributes.
 * US peer-to-peer payment network operated by Early Warning Services.
 */
public class ZelleSpecific {

    @Data
    @Builder
    public static class ZellePayment {
        /** Recipient token (email or mobile) */
        private String recipientToken;
        
        /** Token type */
        private TokenType tokenType;
        
        /** Sender name */
        private String senderName;
        
        /** Recipient name */
        private String recipientName;
        
        /** Payment description/memo */
        private String description;
        
        /** Transaction type */
        private TransactionType transactionType;
        
        /** Payment request ID (for request money feature) */
        private String paymentRequestId;
        
        /** Split payment information */
        private SplitPaymentInfo splitPaymentInfo;
        
        /** Enrollment status */
        private EnrollmentStatus enrollmentStatus;
    }

    @Data
    @Builder
    public static class SplitPaymentInfo {
        /** Is this a split payment */
        private boolean isSplitPayment;
        
        /** Parent transaction ID */
        private String parentTransactionId;
        
        /** Split payment participants */
        private Integer numberOfParticipants;
        
        /** This participant's share */
        private String shareAmount;
    }

    public enum TokenType {
        /** Email address */
        EMAIL,
        
        /** Mobile phone number */
        MOBILE,
        
        /** Zelle tag */
        ZELLE_TAG
    }

    public enum TransactionType {
        /** Send money */
        SEND,
        
        /** Request money */
        REQUEST,
        
        /** Split payment */
        SPLIT
    }

    public enum EnrollmentStatus {
        /** User is enrolled */
        ENROLLED,
        
        /** User is not enrolled */
        NOT_ENROLLED,
        
        /** Enrollment pending */
        PENDING
    }
}
