/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Money domain model.
 * Validates the core business proposition: immutable monetary values with currency.
 */
@DisplayName("Money Domain Model Tests")
class MoneyTest {

    @Nested
    @DisplayName("Creation and Validation")
    class CreationTests {

        @Test
        @DisplayName("Should create money with valid positive amount")
        void shouldCreateMoneyWithPositiveAmount() {
            // Given & When
            Money money = new Money(new BigDecimal("100.50"), Currency.USD);
            
            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
            assertThat(money.getCurrency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("Should create money with zero amount")
        void shouldCreateMoneyWithZeroAmount() {
            // Given & When
            Money money = new Money(BigDecimal.ZERO, Currency.EUR);
            
            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(money.getCurrency()).isEqualTo(Currency.EUR);
        }

        @Test
        @DisplayName("Should reject negative amounts")
        void shouldRejectNegativeAmounts() {
            // Given & When & Then
            assertThatThrownBy(() -> new Money(new BigDecimal("-10.00"), Currency.USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount() {
            // Given & When & Then
            assertThatThrownBy(() -> new Money(null, Currency.USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Should reject null currency")
        void shouldRejectNullCurrency() {
            // Given & When & Then
            assertThatThrownBy(() -> new Money(new BigDecimal("100"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency must not be null");
        }
    }

    @Nested
    @DisplayName("Cents Conversion")
    class CentsConversionTests {

        @Test
        @DisplayName("Should convert amount to cents")
        void shouldConvertAmountToCents() {
            // Given
            Money money = new Money(new BigDecimal("10.50"), Currency.USD);
            
            // When
            long cents = money.toCents();
            
            // Then
            assertThat(cents).isEqualTo(1050L);
        }

        @Test
        @DisplayName("Should create money from cents")
        void shouldCreateMoneyFromCents() {
            // Given & When
            Money money = Money.fromCents(1050L, Currency.USD);
            
            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("10.50"));
            assertThat(money.getCurrency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("Should handle round-trip conversion")
        void shouldHandleRoundTripConversion() {
            // Given
            Money original = new Money(new BigDecimal("123.45"), Currency.EUR);
            
            // When
            long cents = original.toCents();
            Money reconstructed = Money.fromCents(cents, Currency.EUR);
            
            // Then
            assertThat(reconstructed.getAmount()).isEqualByComparingTo(original.getAmount());
            assertThat(reconstructed.getCurrency()).isEqualTo(original.getCurrency());
        }
    }

    @Nested
    @DisplayName("Equality and Immutability")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when amount and currency match")
        void shouldBeEqualWhenAmountAndCurrencyMatch() {
            // Given
            Money money1 = new Money(new BigDecimal("100.00"), Currency.USD);
            Money money2 = new Money(new BigDecimal("100.00"), Currency.USD);
            
            // When & Then
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different amounts")
        void shouldNotBeEqualWithDifferentAmounts() {
            // Given
            Money money1 = new Money(new BigDecimal("100.00"), Currency.USD);
            Money money2 = new Money(new BigDecimal("200.00"), Currency.USD);
            
            // When & Then
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should not be equal with different currencies")
        void shouldNotBeEqualWithDifferentCurrencies() {
            // Given
            Money money1 = new Money(new BigDecimal("100.00"), Currency.USD);
            Money money2 = new Money(new BigDecimal("100.00"), Currency.EUR);
            
            // When & Then
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should be immutable")
        void shouldBeImmutable() {
            // Given
            Money money = new Money(new BigDecimal("100.00"), Currency.USD);
            BigDecimal originalAmount = money.getAmount();
            Currency originalCurrency = money.getCurrency();
            
            // When - try to get references (should be safe)
            BigDecimal amount = money.getAmount();
            Currency currency = money.getCurrency();
            
            // Then - original values unchanged
            assertThat(money.getAmount()).isEqualByComparingTo(originalAmount);
            assertThat(money.getCurrency()).isEqualTo(originalCurrency);
        }
    }

    @Nested
    @DisplayName("Business Scenarios")
    class BusinessScenarioTests {

        @Test
        @DisplayName("Should handle typical payment amounts")
        void shouldHandleTypicalPaymentAmounts() {
            // Small payment
            Money small = new Money(new BigDecimal("9.99"), Currency.USD);
            assertThat(small.getAmount()).isEqualByComparingTo(new BigDecimal("9.99"));
            
            // Medium payment
            Money medium = new Money(new BigDecimal("1234.56"), Currency.EUR);
            assertThat(medium.getAmount()).isEqualByComparingTo(new BigDecimal("1234.56"));
            
            // Large payment
            Money large = new Money(new BigDecimal("999999.99"), Currency.GBP);
            assertThat(large.getAmount()).isEqualByComparingTo(new BigDecimal("999999.99"));
        }

        @Test
        @DisplayName("Should work with different currencies")
        void shouldWorkWithDifferentCurrencies() {
            // Major currencies
            Money usd = new Money(new BigDecimal("100"), Currency.USD);
            Money eur = new Money(new BigDecimal("100"), Currency.EUR);
            Money gbp = new Money(new BigDecimal("100"), Currency.GBP);
            Money jpy = new Money(new BigDecimal("100"), Currency.JPY);
            
            assertThat(usd.getCurrency()).isEqualTo(Currency.USD);
            assertThat(eur.getCurrency()).isEqualTo(Currency.EUR);
            assertThat(gbp.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(jpy.getCurrency()).isEqualTo(Currency.JPY);
        }

        @Test
        @DisplayName("Should maintain precision for financial calculations")
        void shouldMaintainPrecisionForFinancialCalculations() {
            // Business value: No rounding errors in financial amounts
            Money money = new Money(new BigDecimal("100.999"), Currency.USD);
            
            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.999"));
            assertThat(money.getAmount().scale()).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            Money money = new Money(new BigDecimal("100.50"), Currency.USD);
            
            // When
            String toString = money.toString();
            
            // Then
            assertThat(toString).contains("100.50");
            assertThat(toString).contains("USD");
        }
    }
}
