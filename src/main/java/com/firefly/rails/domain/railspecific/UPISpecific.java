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
 * UPI specific attributes.
 * Unified Payments Interface - India's instant payment system.
 */
public class UPISpecific {

    @Data
    @Builder
    public static class UPIPayment {
        /** Virtual Payment Address (VPA) - format: user@bank */
        private String virtualPaymentAddress;
        
        /** UPI Transaction ID */
        private String upiTransactionId;
        
        /** NPCI Transaction ID */
        private String npciTransactionId;
        
        /** Customer reference */
        private String customerReference;
        
        /** Merchant category code */
        private String merchantCategoryCode;
        
        /** Merchant ID */
        private String merchantId;
        
        /** Transaction type */
        private TransactionType transactionType;
        
        /** Payment mode */
        private PaymentMode paymentMode;
        
        /** UPI QR code */
        private String qrCode;
        
        /** Intent information */
        private IntentInfo intentInfo;
        
        /** Mandate information for recurring payments */
        private MandateInfo mandateInfo;
    }

    @Data
    @Builder
    public static class IntentInfo {
        /** Intent URI */
        private String intentUri;
        
        /** Transaction note */
        private String transactionNote;
        
        /** Payee VPA */
        private String payeeVPA;
        
        /** Payee name */
        private String payeeName;
    }

    @Data
    @Builder
    public static class MandateInfo {
        /** Unique Mandate Number (UMN) */
        private String uniqueMandateNumber;
        
        /** Mandate type */
        private MandateType mandateType;
        
        /** Recurrence rule */
        private RecurrenceRule recurrenceRule;
        
        /** Start date */
        private String startDate;
        
        /** End date */
        private String endDate;
        
        /** Amount limit */
        private String amountLimit;
        
        /** Revoke after */
        private Integer revokeAfter;
    }

    @Data
    @Builder
    public static class RecurrenceRule {
        /** Recurrence pattern (DAILY, WEEKLY, MONTHLY, YEARLY) */
        private RecurrencePattern pattern;
        
        /** Recurrence value */
        private Integer value;
    }

    public enum TransactionType {
        /** Person to Person */
        P2P,
        
        /** Person to Merchant */
        P2M,
        
        /** Merchant to Person (refunds) */
        M2P
    }

    public enum PaymentMode {
        /** Default mode */
        DEFAULT,
        
        /** QR code scan */
        QR,
        
        /** Intent based */
        INTENT,
        
        /** Collect request */
        COLLECT
    }

    public enum MandateType {
        /** Create mandate */
        CREATE,
        
        /** Revoke mandate */
        REVOKE,
        
        /** Execute mandate */
        EXECUTE
    }

    public enum RecurrencePattern {
        DAILY,
        WEEKLY,
        FORTNIGHTLY,
        MONTHLY,
        QUARTERLY,
        HALF_YEARLY,
        YEARLY,
        AS_PRESENTED
    }
}
