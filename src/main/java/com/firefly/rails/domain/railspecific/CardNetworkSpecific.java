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
 * Card Network specific attributes.
 * Visa, Mastercard, AmEx, Discover, etc. payment processing.
 */
public class CardNetworkSpecific {

    @Data
    @Builder
    public static class CardPayment {
        /** Card network (VISA, MASTERCARD, AMEX, DISCOVER) */
        private CardNetwork cardNetwork;
        
        /** Card number (PAN) - tokenized */
        private String cardToken;
        
        /** Card expiry month */
        private String expiryMonth;
        
        /** Card expiry year */
        private String expiryYear;
        
        /** CVV/CVC */
        private String cvv;
        
        /** Cardholder name */
        private String cardholderName;
        
        /** Billing address */
        private BillingAddress billingAddress;
        
        /** Transaction type */
        private TransactionType transactionType;
        
        /** 3D Secure information */
        private ThreeDSecureInfo threeDSecureInfo;
        
        /** Merchant category code (MCC) */
        private String merchantCategoryCode;
        
        /** Merchant ID */
        private String merchantId;
        
        /** Terminal ID */
        private String terminalId;
        
        /** Authorization code */
        private String authorizationCode;
        
        /** Retrieval reference number */
        private String retrievalReferenceNumber;
        
        /** Interchange information */
        private InterchangeInfo interchangeInfo;
    }

    @Data
    @Builder
    public static class BillingAddress {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    @Data
    @Builder
    public static class ThreeDSecureInfo {
        /** 3DS version (1.0, 2.0, 2.1, 2.2) */
        private String version;
        
        /** Authentication status */
        private AuthenticationStatus authenticationStatus;
        
        /** ECI (Electronic Commerce Indicator) */
        private String eci;
        
        /** CAVV (Cardholder Authentication Verification Value) */
        private String cavv;
        
        /** XID (Transaction identifier) */
        private String xid;
        
        /** Directory server transaction ID */
        private String dsTransactionId;
        
        /** ACS transaction ID */
        private String acsTransactionId;
    }

    @Data
    @Builder
    public static class InterchangeInfo {
        /** Interchange rate */
        private String interchangeRate;
        
        /** Interchange category */
        private String interchangeCategory;
        
        /** Downgrade reason */
        private String downgradeReason;
    }

    public enum CardNetwork {
        VISA,
        MASTERCARD,
        AMEX,
        DISCOVER,
        DINERS,
        JCB,
        UNIONPAY
    }

    public enum TransactionType {
        /** Purchase/Sale */
        PURCHASE,
        
        /** Authorization only */
        AUTHORIZATION,
        
        /** Capture */
        CAPTURE,
        
        /** Refund */
        REFUND,
        
        /** Void */
        VOID,
        
        /** Chargeback */
        CHARGEBACK,
        
        /** Recurring */
        RECURRING
    }

    public enum AuthenticationStatus {
        /** Authentication successful */
        AUTHENTICATED,
        
        /** Authentication attempted */
        ATTEMPTED,
        
        /** Authentication failed */
        FAILED,
        
        /** Not authenticated */
        NOT_AUTHENTICATED,
        
        /** Unable to authenticate */
        UNABLE_TO_AUTHENTICATE
    }
}
