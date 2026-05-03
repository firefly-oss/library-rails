/*
 * Copyright 2025 Firefly Software Foundation
 */

package com.firefly.rails.util;

import com.firefly.rails.domain.BankAccount;
import com.firefly.rails.domain.Currency;
import com.firefly.rails.domain.Money;
import com.firefly.rails.exceptions.InvalidAccountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilTest {
    
    @Test
    void testIsValidIBAN_ValidIBANs() {
        // Valid IBANs from different countries
        assertThat(ValidationUtil.isValidIBAN("GB82WEST12345698765432")).isTrue();
        assertThat(ValidationUtil.isValidIBAN("DE89370400440532013000")).isTrue();
        assertThat(ValidationUtil.isValidIBAN("FR1420041010050500013M02606")).isTrue();
        assertThat(ValidationUtil.isValidIBAN("IT60X0542811101000000123456")).isTrue();
        assertThat(ValidationUtil.isValidIBAN("ES9121000418450200051332")).isTrue();
    }
    
    @Test
    void testIsValidIBAN_InvalidIBANs() {
        assertThat(ValidationUtil.isValidIBAN(null)).isFalse();
        assertThat(ValidationUtil.isValidIBAN("")).isFalse();
        assertThat(ValidationUtil.isValidIBAN("INVALID")).isFalse();
        assertThat(ValidationUtil.isValidIBAN("GB82WEST1234569876543X")).isFalse(); // Invalid checksum
        assertThat(ValidationUtil.isValidIBAN("DE893704004405320130")).isFalse(); // Wrong length
    }
    
    @Test
    void testIsValidIBAN_WithSpaces() {
        assertThat(ValidationUtil.isValidIBAN("GB82 WEST 1234 5698 7654 32")).isTrue();
        assertThat(ValidationUtil.isValidIBAN("DE89 3704 0044 0532 0130 00")).isTrue();
    }
    
    @Test
    void testIsValidBIC_Valid() {
        assertThat(ValidationUtil.isValidBIC("DEUTDEFF")).isTrue();
        assertThat(ValidationUtil.isValidBIC("DEUTDEFFXXX")).isTrue();
        assertThat(ValidationUtil.isValidBIC("ABNAGB2L")).isTrue();
        assertThat(ValidationUtil.isValidBIC("CHASGB2L")).isTrue();
    }
    
    @Test
    void testIsValidBIC_Invalid() {
        assertThat(ValidationUtil.isValidBIC(null)).isFalse();
        assertThat(ValidationUtil.isValidBIC("")).isFalse();
        assertThat(ValidationUtil.isValidBIC("INVALID")).isFalse();
        assertThat(ValidationUtil.isValidBIC("DEUT")).isFalse(); // Too short
        assertThat(ValidationUtil.isValidBIC("123456789")).isFalse(); // Numbers only
    }
    
    @Test
    void testIsValidUSRoutingNumber_Valid() {
        assertThat(ValidationUtil.isValidUSRoutingNumber("021000021")).isTrue(); // Chase
        assertThat(ValidationUtil.isValidUSRoutingNumber("026009593")).isTrue(); // Bank of America
        assertThat(ValidationUtil.isValidUSRoutingNumber("121000248")).isTrue(); // Wells Fargo
    }
    
    @Test
    void testIsValidUSRoutingNumber_Invalid() {
        assertThat(ValidationUtil.isValidUSRoutingNumber(null)).isFalse();
        assertThat(ValidationUtil.isValidUSRoutingNumber("")).isFalse();
        assertThat(ValidationUtil.isValidUSRoutingNumber("123456789")).isFalse(); // Invalid checksum
        assertThat(ValidationUtil.isValidUSRoutingNumber("12345678")).isFalse(); // Too short
        assertThat(ValidationUtil.isValidUSRoutingNumber("1234567890")).isFalse(); // Too long
    }
    
    @Test
    void testValidateAmount_Valid() {
        Money money = new Money(BigDecimal.valueOf(100.50), Currency.USD);
        
        // Should not throw
        ValidationUtil.validateAmount(money);
    }
    
    @Test
    void testValidateAmount_Null() {
        assertThatThrownBy(() -> ValidationUtil.validateAmount(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Amount cannot be null");
    }
    
    @Test
    void testValidateAmount_NullValue() {
        Money money = new Money(null, Currency.USD);
        
        assertThatThrownBy(() -> ValidationUtil.validateAmount(money))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Amount value cannot be null");
    }
    
    @Test
    void testValidateAmount_ZeroOrNegative() {
        Money zeroMoney = new Money(BigDecimal.ZERO, Currency.USD);
        Money negativeMoney = new Money(BigDecimal.valueOf(-10), Currency.USD);
        
        assertThatThrownBy(() -> ValidationUtil.validateAmount(zeroMoney))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Amount must be positive");
        
        assertThatThrownBy(() -> ValidationUtil.validateAmount(negativeMoney))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Amount must be positive");
    }
    
    @Test
    void testValidateAmount_NullCurrency() {
        Money money = new Money(BigDecimal.valueOf(100), null);
        
        assertThatThrownBy(() -> ValidationUtil.validateAmount(money))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Currency cannot be null");
    }
    
    @Test
    void testValidateBankAccount_ValidIBAN() {
        BankAccount account = BankAccount.fromIban(
            "John Doe",
            "GB82WEST12345698765432",
            "DEUTDEFF"
        );
        
        // Should not throw
        ValidationUtil.validateBankAccount(account);
    }
    
    @Test
    void testValidateBankAccount_ValidAccountNumber() {
        BankAccount account = BankAccount.fromAccountNumber(
            "Jane Smith",
            "1234567890",
            "021000021"
        );
        
        // Should not throw
        ValidationUtil.validateBankAccount(account);
    }
    
    @Test
    void testValidateBankAccount_Null() {
        assertThatThrownBy(() -> ValidationUtil.validateBankAccount(null))
            .isInstanceOf(InvalidAccountException.class)
            .hasMessageContaining("Bank account cannot be null");
    }
    
    @Test
    void testValidateBankAccount_NoAccountHolderName() {
        BankAccount account = BankAccount.fromIban(
            null,
            "GB82WEST12345698765432",
            "DEUTDEFF"
        );
        
        assertThatThrownBy(() -> ValidationUtil.validateBankAccount(account))
            .isInstanceOf(InvalidAccountException.class)
            .hasMessageContaining("Account holder name is required");
    }
    
    @Test
    void testValidateBankAccount_InvalidIBAN() {
        BankAccount account = BankAccount.fromIban(
            "John Doe",
            "INVALID_IBAN",
            "DEUTDEFF"
        );
        
        assertThatThrownBy(() -> ValidationUtil.validateBankAccount(account))
            .isInstanceOf(InvalidAccountException.class)
            .hasMessageContaining("Invalid IBAN");
    }
    
    @Test
    void testValidateBankAccount_AccountNumberTooShort() {
        BankAccount account = BankAccount.fromAccountNumber(
            "Jane Smith",
            "123", // Too short
            "021000021"
        );
        
        assertThatThrownBy(() -> ValidationUtil.validateBankAccount(account))
            .isInstanceOf(InvalidAccountException.class)
            .hasMessageContaining("Account number too short");
    }
    
    @Test
    void testValidateBankAccount_NoAccountIdentifier() {
        BankAccount account = new BankAccount("John Doe", null, null, null, null, null, null);
        
        assertThatThrownBy(() -> ValidationUtil.validateBankAccount(account))
            .isInstanceOf(InvalidAccountException.class)
            .hasMessageContaining("Either IBAN or account number must be provided");
    }
    
    @Test
    void testValidationUtil_CannotInstantiate() {
        assertThatThrownBy(() -> {
            var constructor = ValidationUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .getCause()
        .hasMessageContaining("Utility class cannot be instantiated");
    }
}
