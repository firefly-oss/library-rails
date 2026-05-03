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
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Value object representing a bank account.
 * Supports multiple account number formats (IBAN, account/routing, etc.)
 */
@Getter
@EqualsAndHashCode
@ToString
public class BankAccount {

    /** Account holder name */
    @NotBlank
    private final String accountHolderName;

    /** IBAN (International Bank Account Number) - for SEPA, SWIFT */
    private final String iban;

    /** Account number - for domestic transfers */
    private final String accountNumber;

    /** Routing/Sort Code/BSB - varies by country */
    private final String routingNumber;

    /** SWIFT/BIC code - for international transfers */
    private final String swiftCode;

    /** Bank name */
    private final String bankName;

    /** Country code (ISO 3166-1 alpha-2) */
    private final String countryCode;

    @JsonCreator
    public BankAccount(
            @JsonProperty("accountHolderName") String accountHolderName,
            @JsonProperty("iban") String iban,
            @JsonProperty("accountNumber") String accountNumber,
            @JsonProperty("routingNumber") String routingNumber,
            @JsonProperty("swiftCode") String swiftCode,
            @JsonProperty("bankName") String bankName,
            @JsonProperty("countryCode") String countryCode) {
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new IllegalArgumentException("Account holder name must not be blank");
        }
        this.accountHolderName = accountHolderName;
        this.iban = iban;
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.countryCode = countryCode;
    }

    /**
     * Create a bank account with IBAN (for SEPA/European transfers).
     */
    public static BankAccount fromIban(String accountHolderName, String iban, String swiftCode) {
        return new BankAccount(accountHolderName, iban, null, null, swiftCode, null, null);
    }

    /**
     * Create a bank account with account number and routing number (for US ACH transfers).
     */
    public static BankAccount fromAccountNumber(String accountHolderName, String accountNumber, String routingNumber) {
        return new BankAccount(accountHolderName, null, accountNumber, routingNumber, null, null, "US");
    }
}
