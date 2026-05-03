/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * Idempotency key for safe payment retries.
 * 
 * Ensures that retrying the same payment operation doesn't result in duplicate payments.
 * Critical for network failures, timeouts, and other transient errors.
 */
@Getter
@EqualsAndHashCode
@ToString
public class IdempotencyKey {

    /** Unique idempotency key */
    private final String key;

    /** When the key was created */
    private final Instant createdAt;

    /** Key expiry time (after which it can be reused) */
    private final Instant expiresAt;

    @JsonCreator
    public IdempotencyKey(
            @JsonProperty("key") String key,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("expiresAt") Instant expiresAt) {
        this.key = key;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.expiresAt = expiresAt;
    }

    /**
     * Generate a new idempotency key with 24-hour expiry.
     */
    public static IdempotencyKey generate() {
        return new IdempotencyKey(
                UUID.randomUUID().toString(),
                Instant.now(),
                Instant.now().plusSeconds(86400) // 24 hours
        );
    }

    /**
     * Create idempotency key from string.
     */
    public static IdempotencyKey fromString(String key) {
        return new IdempotencyKey(key, Instant.now(), Instant.now().plusSeconds(86400));
    }

    /**
     * Check if key has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
