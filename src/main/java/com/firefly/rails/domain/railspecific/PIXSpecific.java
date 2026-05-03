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
 * PIX specific attributes.
 * Brazilian instant payment system operated by Banco Central do Brasil.
 */
public class PIXSpecific {

    @Data
    @Builder
    public static class PIXPayment {
        /** PIX Key (CPF, CNPJ, Email, Phone, or Random) */
        private String pixKey;
        
        /** PIX Key type */
        private PIXKeyType pixKeyType;
        
        /** End-to-End ID (E2E ID) */
        private String endToEndId;
        
        /** Transaction ID (txid) for QR Code */
        private String transactionId;
        
        /** Payment description */
        private String description;
        
        /** PIX QR Code payload */
        private String qrCodePayload;
        
        /** Initiator type */
        private InitiatorType initiatorType;
        
        /** Return/Chargeback information */
        private ReturnInfo returnInfo;
        
        /** DICT (Directory of identifiers) information */
        private DICTInfo dictInfo;
    }

    @Data
    @Builder
    public static class ReturnInfo {
        /** Original E2E ID being returned */
        private String originalE2EId;
        
        /** Return reason */
        private ReturnReason returnReason;
        
        /** Return description */
        private String returnDescription;
    }

    @Data
    @Builder
    public static class DICTInfo {
        /** Account holder document (CPF/CNPJ) */
        private String accountHolderDocument;
        
        /** Account holder name */
        private String accountHolderName;
        
        /** Bank ISPB (Identifier of the payment system participant) */
        private String ispb;
        
        /** Branch code */
        private String branchCode;
        
        /** Account number */
        private String accountNumber;
        
        /** Account type */
        private AccountType accountType;
    }

    public enum PIXKeyType {
        /** CPF (Individual taxpayer ID) */
        CPF,
        
        /** CNPJ (Corporate taxpayer ID) */
        CNPJ,
        
        /** Email address */
        EMAIL,
        
        /** Phone number */
        PHONE,
        
        /** Random key (UUID) */
        RANDOM
    }

    public enum InitiatorType {
        /** Payer initiated */
        PAYER,
        
        /** Payee initiated (QR Code) */
        PAYEE
    }

    public enum ReturnReason {
        /** Duplicate payment */
        DUPLICATE,
        
        /** Fraud */
        FRAUD,
        
        /** Processing error */
        ERROR,
        
        /** Customer request */
        CUSTOMER_REQUEST
    }

    public enum AccountType {
        /** Checking account */
        CHECKING,
        
        /** Savings account */
        SAVINGS,
        
        /** Payment account */
        PAYMENT
    }
}
