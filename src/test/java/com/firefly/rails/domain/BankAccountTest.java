/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BankAccount domain model.
 * Validates support for multiple account formats: IBAN, Account/Routing, SWIFT.
 */
@DisplayName("BankAccount Domain Model Tests")
class BankAccountTest {

    @Nested
    @DisplayName("IBAN Account Creation")
    class IBANAccountTests {

        @Test
        @DisplayName("Should create account with IBAN and SWIFT")
        void shouldCreateAccountWithIBANAndSWIFT() {
            // Given & When
            BankAccount account = BankAccount.fromIban(
                "John Doe",
                "GB82WEST12345698765432",
                "ABCDEFGH"
            );
            
            // Then
            assertThat(account.getAccountHolderName()).isEqualTo("John Doe");
            assertThat(account.getIban()).isEqualTo("GB82WEST12345698765432");
            assertThat(account.getSwiftCode()).isEqualTo("ABCDEFGH");
            assertThat(account.getAccountNumber()).isNull();
            assertThat(account.getRoutingNumber()).isNull();
        }

        @Test
        @DisplayName("Should support European SEPA IBANs")
        void shouldSupportEuropeanSEPAIBANs() {
            // German IBAN
            BankAccount german = BankAccount.fromIban(
                "German Company",
                "DE89370400440532013000",
                "COBADEFFXXX"
            );
            assertThat(german.getIban()).startsWith("DE");
            
            // French IBAN
            BankAccount french = BankAccount.fromIban(
                "French Company",
                "FR1420041010050500013M02606",
                "BNPAFRPPXXX"
            );
            assertThat(french.getIban()).startsWith("FR");
        }

        @Test
        @DisplayName("Should reject empty account holder name")
        void shouldRejectEmptyAccountHolderName() {
            // Given & When & Then
            assertThatThrownBy(() -> 
                BankAccount.fromIban("", "GB82WEST12345698765432", "ABCDEFGH")
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Account holder name must not be blank");
        }

        @Test
        @DisplayName("Should reject null account holder name")
        void shouldRejectNullAccountHolderName() {
            // Given & When & Then
            assertThatThrownBy(() -> 
                BankAccount.fromIban(null, "GB82WEST12345698765432", "ABCDEFGH")
            )
            .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Account/Routing Number Creation")
    class AccountRoutingTests {

        @Test
        @DisplayName("Should create account with account and routing numbers")
        void shouldCreateAccountWithAccountAndRoutingNumbers() {
            // Given & When
            BankAccount account = BankAccount.fromAccountNumber(
                "Jane Smith",
                "123456789",
                "021000021"
            );
            
            // Then
            assertThat(account.getAccountHolderName()).isEqualTo("Jane Smith");
            assertThat(account.getAccountNumber()).isEqualTo("123456789");
            assertThat(account.getRoutingNumber()).isEqualTo("021000021");
            assertThat(account.getCountryCode()).isEqualTo("US");
            assertThat(account.getIban()).isNull();
            assertThat(account.getSwiftCode()).isNull();
        }

        @Test
        @DisplayName("Should support US ACH routing numbers")
        void shouldSupportUSACHRoutingNumbers() {
            // Chase routing number
            BankAccount chase = BankAccount.fromAccountNumber(
                "Chase Customer",
                "123456789",
                "021000021"
            );
            assertThat(chase.getRoutingNumber()).hasSize(9);
            
            // Bank of America routing number
            BankAccount boa = BankAccount.fromAccountNumber(
                "BOA Customer",
                "987654321",
                "026009593"
            );
            assertThat(boa.getRoutingNumber()).hasSize(9);
        }
    }

    @Nested
    @DisplayName("Full Constructor Tests")
    class FullConstructorTests {

        @Test
        @DisplayName("Should create account with all fields")
        void shouldCreateAccountWithAllFields() {
            // Given & When
            BankAccount account = new BankAccount(
                "Corporate Account",
                "GB82WEST12345698765432",  // IBAN
                "12345678",                 // Account number
                "123456",                   // Routing
                "ABCDEFGH",                 // SWIFT
                "Test Bank",                // Bank name
                "GB"                        // Country
            );
            
            // Then
            assertThat(account.getAccountHolderName()).isEqualTo("Corporate Account");
            assertThat(account.getIban()).isEqualTo("GB82WEST12345698765432");
            assertThat(account.getAccountNumber()).isEqualTo("12345678");
            assertThat(account.getRoutingNumber()).isEqualTo("123456");
            assertThat(account.getSwiftCode()).isEqualTo("ABCDEFGH");
            assertThat(account.getBankName()).isEqualTo("Test Bank");
            assertThat(account.getCountryCode()).isEqualTo("GB");
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal with same IBAN")
        void shouldBeEqualWithSameIBAN() {
            // Given
            BankAccount account1 = BankAccount.fromIban(
                "John Doe",
                "GB82WEST12345698765432",
                "ABCDEFGH"
            );
            BankAccount account2 = BankAccount.fromIban(
                "John Doe",
                "GB82WEST12345698765432",
                "ABCDEFGH"
            );
            
            // When & Then
            assertThat(account1).isEqualTo(account2);
            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different IBANs")
        void shouldNotBeEqualWithDifferentIBANs() {
            // Given
            BankAccount account1 = BankAccount.fromIban(
                "John Doe",
                "GB82WEST12345698765432",
                "ABCDEFGH"
            );
            BankAccount account2 = BankAccount.fromIban(
                "John Doe",
                "FR1420041010050500013M02606",
                "BNPAFRPP"
            );
            
            // When & Then
            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("Should be equal with same account/routing numbers")
        void shouldBeEqualWithSameAccountRoutingNumbers() {
            // Given
            BankAccount account1 = BankAccount.fromAccountNumber(
                "Jane Smith",
                "123456789",
                "021000021"
            );
            BankAccount account2 = BankAccount.fromAccountNumber(
                "Jane Smith",
                "123456789",
                "021000021"
            );
            
            // When & Then
            assertThat(account1).isEqualTo(account2);
        }
    }

    @Nested
    @DisplayName("Business Scenarios")
    class BusinessScenarioTests {

        @Test
        @DisplayName("Should support SEPA credit transfer scenario")
        void shouldSupportSEPACreditTransferScenario() {
            // Business value: Cross-border European payments
            BankAccount debtor = BankAccount.fromIban(
                "German Company GmbH",
                "DE89370400440532013000",
                "COBADEFFXXX"
            );
            
            BankAccount creditor = BankAccount.fromIban(
                "French Company SARL",
                "FR1420041010050500013M02606",
                "BNPAFRPPXXX"
            );
            
            assertThat(debtor.getIban()).isNotNull();
            assertThat(creditor.getIban()).isNotNull();
            assertThat(debtor.getSwiftCode()).isNotNull();
            assertThat(creditor.getSwiftCode()).isNotNull();
        }

        @Test
        @DisplayName("Should support US domestic ACH scenario")
        void shouldSupportUSDomesticACHScenario() {
            // Business value: US domestic bank transfers
            BankAccount debtor = BankAccount.fromAccountNumber(
                "US Company Inc",
                "123456789",
                "021000021" // Chase routing
            );
            
            BankAccount creditor = BankAccount.fromAccountNumber(
                "US Vendor LLC",
                "987654321",
                "026009593" // Bank of America routing
            );
            
            assertThat(debtor.getAccountNumber()).isNotNull();
            assertThat(creditor.getAccountNumber()).isNotNull();
            assertThat(debtor.getRoutingNumber()).hasSize(9);
            assertThat(creditor.getRoutingNumber()).hasSize(9);
            assertThat(debtor.getCountryCode()).isEqualTo("US");
        }

        @Test
        @DisplayName("Should support UK Faster Payments scenario")
        void shouldSupportUKFasterPaymentsScenario() {
            // Business value: UK instant payments
            BankAccount account = BankAccount.fromIban(
                "UK Company Ltd",
                "GB82WEST12345698765432",
                "NWBKGB2L"
            );
            
            assertThat(account.getIban()).startsWith("GB");
            assertThat(account.getSwiftCode()).isNotNull();
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            BankAccount account = BankAccount.fromIban(
                "John Doe",
                "GB82WEST12345698765432",
                "ABCDEFGH"
            );
            
            // When
            String toString = account.toString();
            
            // Then
            assertThat(toString).contains("John Doe");
            assertThat(toString).contains("GB82WEST12345698765432");
        }
    }
}
