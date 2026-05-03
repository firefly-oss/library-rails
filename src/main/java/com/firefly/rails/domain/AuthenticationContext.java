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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Strong Customer Authentication (SCA) context.
 * Contains information about the authentication flow for PSD2/regulatory compliance.
 */
@Getter
@EqualsAndHashCode
@ToString
public class AuthenticationContext {

    /** Required authentication method */
    private final AuthenticationMethod authenticationMethod;

    /** Authentication challenge/token */
    private final String authenticationToken;

    /** Redirect URL for OAuth/3DS flows */
    private final String redirectUrl;

    /** Callback URL after authentication */
    private final String callbackUrl;

    /** User IP address (for fraud detection) */
    private final String userIpAddress;

    /** User device fingerprint */
    private final String deviceFingerprint;

    /** Authentication expiry time */
    private final Instant expiresAt;

    /** Whether this is an SCA exemption scenario */
    private final boolean scaExemption;

    /** Exemption reason if applicable */
    private final String exemptionReason;

    @JsonCreator
    public AuthenticationContext(
            @JsonProperty("authenticationMethod") AuthenticationMethod authenticationMethod,
            @JsonProperty("authenticationToken") String authenticationToken,
            @JsonProperty("redirectUrl") String redirectUrl,
            @JsonProperty("callbackUrl") String callbackUrl,
            @JsonProperty("userIpAddress") String userIpAddress,
            @JsonProperty("deviceFingerprint") String deviceFingerprint,
            @JsonProperty("expiresAt") Instant expiresAt,
            @JsonProperty("scaExemption") boolean scaExemption,
            @JsonProperty("exemptionReason") String exemptionReason) {
        this.authenticationMethod = authenticationMethod;
        this.authenticationToken = authenticationToken;
        this.redirectUrl = redirectUrl;
        this.callbackUrl = callbackUrl;
        this.userIpAddress = userIpAddress;
        this.deviceFingerprint = deviceFingerprint;
        this.expiresAt = expiresAt;
        this.scaExemption = scaExemption;
        this.exemptionReason = exemptionReason;
    }

    /**
     * Create simple authentication context with OTP.
     */
    public static AuthenticationContext withOtp(String token, String callbackUrl) {
        return new AuthenticationContext(
                AuthenticationMethod.SMS_OTP,
                token,
                null,
                callbackUrl,
                null,
                null,
                Instant.now().plusSeconds(300), // 5 minutes
                false,
                null
        );
    }

    /**
     * Create authentication context with redirect (OAuth/3DS).
     */
    public static AuthenticationContext withRedirect(String redirectUrl, String callbackUrl) {
        return new AuthenticationContext(
                AuthenticationMethod.OAUTH_REDIRECT,
                null,
                redirectUrl,
                callbackUrl,
                null,
                null,
                Instant.now().plusSeconds(600), // 10 minutes
                false,
                null
        );
    }

    /**
     * Create SCA exemption context.
     */
    public static AuthenticationContext withExemption(String exemptionReason) {
        return new AuthenticationContext(
                AuthenticationMethod.NONE,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                exemptionReason
        );
    }
}
