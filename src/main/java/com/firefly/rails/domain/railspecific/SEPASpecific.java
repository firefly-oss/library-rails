/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.domain.railspecific;

import lombok.Builder;
import lombok.Data;

/**
 * SEPA-specific payment attributes.
 */
public class SEPASpecific {

    @Data
    @Builder
    public static class SEPACreditTransfer {
        /** SEPA Scheme (SCT, SCT Inst) */
        private SEPAScheme scheme;
        
        /** Purpose code (SEPA) */
        private String purposeCode;
        
        /** Creditor reference (structured) */
        private String creditorReference;
        
        /** Category purpose */
        private String categoryPurpose;
        
        /** Service level */
        private String serviceLevel;
    }

    @Data
    @Builder
    public static class SEPADirectDebit {
        /** SEPA Scheme (SDD Core, SDD B2B) */
        private SEPAScheme scheme;
        
        /** Mandate reference */
        private String mandateReference;
        
        /** Mandate signature date */
        private String mandateSignatureDate;
        
        /** Sequence type */
        private SequenceType sequenceType;
        
        /** Creditor ID */
        private String creditorId;
    }

    public enum SEPAScheme {
        /** SEPA Credit Transfer */
        SCT,
        
        /** SEPA Instant Credit Transfer */
        SCT_INST,
        
        /** SEPA Direct Debit Core */
        SDD_CORE,
        
        /** SEPA Direct Debit B2B */
        SDD_B2B
    }

    public enum SequenceType {
        FRST,  // First
        RCUR,  // Recurring
        OOFF,  // One-off
        FNAL   // Final
    }
}
