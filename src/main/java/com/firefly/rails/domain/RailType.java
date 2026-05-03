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

package com.firefly.rails.domain;

/**
 * Enumeration of supported banking payment rails.
 * 
 * Each rail represents a different payment network infrastructure
 * with specific capabilities, settlement times, and geographic coverage.
 */
public enum RailType {
    
    /** Automated Clearing House - US domestic batch transfers */
    ACH,
    
    /** Society for Worldwide Interbank Financial Telecommunication - International wire transfers */
    SWIFT,
    
    /** Single Euro Payments Area - European credit transfers and direct debits */
    SEPA,
    
    /** Clearing House Interbank Payments System - US large-value transfers */
    CHIPS,
    
    /** Faster Payments Service - UK real-time payments */
    FPS,
    
    /** Federal Reserve Wire Network - US real-time gross settlement */
    FEDWIRE,
    
    /** Interac e-Transfer - Canadian electronic funds transfer */
    INTERAC,
    
    /** Real-Time Payments - US instant payment network */
    RTP,
    
    /** Card Networks - Visa, Mastercard, AmEx, etc. */
    CARD_NETWORK,
    
    /** Mobile Payment Systems - Apple Pay, Google Pay, Samsung Pay */
    MOBILE_WALLET,
    
    /** Cryptocurrency/Blockchain Networks - Bitcoin, Ethereum, stablecoins */
    CRYPTO_BLOCKCHAIN,
    
    /** TARGET2 - Trans-European Automated Real-time Gross settlement Express Transfer system */
    TARGET2,
    
    /** BACS - UK Bankers' Automated Clearing Services */
    BACS,
    
    /** PIX - Brazilian instant payment system */
    PIX,
    
    /** UPI - Unified Payments Interface (India) */
    UPI,
    
    /** Zelle - US peer-to-peer payment network */
    ZELLE
}
