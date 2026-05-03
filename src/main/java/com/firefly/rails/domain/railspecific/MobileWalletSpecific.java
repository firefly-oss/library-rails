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
 * Mobile Wallet specific attributes.
 * Apple Pay, Google Pay, Samsung Pay, etc.
 */
public class MobileWalletSpecific {

    @Data
    @Builder
    public static class MobileWalletPayment {
        /** Wallet provider */
        private WalletProvider walletProvider;
        
        /** Payment token (cryptogram) */
        private String paymentToken;
        
        /** Token type */
        private TokenType tokenType;
        
        /** Transaction ID */
        private String transactionId;
        
        /** Device information */
        private DeviceInfo deviceInfo;
        
        /** Tokenization data */
        private TokenizationData tokenizationData;
        
        /** Biometric authentication used */
        private boolean biometricAuthUsed;
        
        /** Transaction certificate */
        private String transactionCertificate;
        
        /** Application data */
        private String applicationData;
    }

    @Data
    @Builder
    public static class DeviceInfo {
        /** Device ID/fingerprint */
        private String deviceId;
        
        /** Device type */
        private DeviceType deviceType;
        
        /** Device manufacturer */
        private String manufacturer;
        
        /** Device model */
        private String model;
        
        /** OS version */
        private String osVersion;
        
        /** Wallet app version */
        private String walletAppVersion;
    }

    @Data
    @Builder
    public static class TokenizationData {
        /** Network token (DPAN) */
        private String networkToken;
        
        /** Token requestor ID */
        private String tokenRequestorId;
        
        /** Token assurance level */
        private String tokenAssuranceLevel;
        
        /** Cryptogram */
        private String cryptogram;
        
        /** ECI (Electronic Commerce Indicator) */
        private String eci;
    }

    public enum WalletProvider {
        /** Apple Pay */
        APPLE_PAY,
        
        /** Google Pay */
        GOOGLE_PAY,
        
        /** Samsung Pay */
        SAMSUNG_PAY,
        
        /** PayPal */
        PAYPAL,
        
        /** Venmo */
        VENMO,
        
        /** Cash App */
        CASH_APP,
        
        /** Amazon Pay */
        AMAZON_PAY,
        
        /** AliPay */
        ALIPAY,
        
        /** WeChat Pay */
        WECHAT_PAY
    }

    public enum TokenType {
        /** Single-use token */
        SINGLE_USE,
        
        /** Multi-use token */
        MULTI_USE,
        
        /** Network token */
        NETWORK_TOKEN
    }

    public enum DeviceType {
        /** Mobile phone */
        MOBILE,
        
        /** Tablet */
        TABLET,
        
        /** Wearable (watch) */
        WEARABLE,
        
        /** Desktop/Web */
        DESKTOP,
        
        /** Smart device */
        SMART_DEVICE
    }
}
