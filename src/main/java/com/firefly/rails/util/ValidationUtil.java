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

package com.firefly.rails.util;

import com.firefly.rails.domain.BankAccount;
import com.firefly.rails.domain.Money;
import com.firefly.rails.exceptions.InvalidAccountException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 * 
 * <p>Provides common validation methods for banking operations including
 * account number validation, IBAN validation, amount validation, etc.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Validate IBAN
 * if (ValidationUtil.isValidIBAN(iban)) {
 *     // Process payment
 * }
 * 
 * // Validate amount
 * ValidationUtil.validateAmount(money);
 * 
 * // Validate account
 * ValidationUtil.validateBankAccount(account);
 * }</pre>
 */
public final class ValidationUtil {
    
    private static final Pattern IBAN_PATTERN = Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]+");
    private static final Pattern BIC_PATTERN = Pattern.compile("[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?");
    private static final Pattern US_ROUTING_PATTERN = Pattern.compile("^[0-9]{9}$");
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates an IBAN (International Bank Account Number).
     * 
     * @param iban The IBAN to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidIBAN(String iban) {
        if (iban == null || iban.isEmpty()) {
            return false;
        }
        
        String normalizedIBAN = iban.replaceAll("\\s", "").toUpperCase();
        
        if (!IBAN_PATTERN.matcher(normalizedIBAN).matches()) {
            return false;
        }
        
        // Length validation (varies by country)
        int expectedLength = getExpectedIBANLength(normalizedIBAN.substring(0, 2));
        if (expectedLength > 0 && normalizedIBAN.length() != expectedLength) {
            return false;
        }
        
        // Checksum validation (mod-97 algorithm)
        return validateIBANChecksum(normalizedIBAN);
    }
    
    /**
     * Validates a BIC/SWIFT code.
     * 
     * @param bic The BIC to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBIC(String bic) {
        if (bic == null || bic.isEmpty()) {
            return false;
        }
        
        return BIC_PATTERN.matcher(bic.toUpperCase()).matches();
    }
    
    /**
     * Validates a US routing number.
     * 
     * @param routingNumber The routing number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUSRoutingNumber(String routingNumber) {
        if (routingNumber == null || !US_ROUTING_PATTERN.matcher(routingNumber).matches()) {
            return false;
        }
        
        // ABA checksum validation
        int sum = 0;
        sum += 3 * Character.getNumericValue(routingNumber.charAt(0));
        sum += 7 * Character.getNumericValue(routingNumber.charAt(1));
        sum += 1 * Character.getNumericValue(routingNumber.charAt(2));
        sum += 3 * Character.getNumericValue(routingNumber.charAt(3));
        sum += 7 * Character.getNumericValue(routingNumber.charAt(4));
        sum += 1 * Character.getNumericValue(routingNumber.charAt(5));
        sum += 3 * Character.getNumericValue(routingNumber.charAt(6));
        sum += 7 * Character.getNumericValue(routingNumber.charAt(7));
        sum += 1 * Character.getNumericValue(routingNumber.charAt(8));
        
        return sum % 10 == 0;
    }
    
    /**
     * Validates a Money amount.
     * 
     * @param amount The amount to validate
     * @throws IllegalArgumentException if invalid
     */
    public static void validateAmount(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        
        BigDecimal value = amount.getAmount();
        if (value == null) {
            throw new IllegalArgumentException("Amount value cannot be null");
        }
        
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (amount.getCurrency() == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
    }
    
    /**
     * Validates a bank account.
     * 
     * @param account The account to validate
     * @throws InvalidAccountException if invalid
     */
    public static void validateBankAccount(BankAccount account) {
        if (account == null) {
            throw new InvalidAccountException("Bank account cannot be null");
        }
        
        if (account.getAccountHolderName() == null || account.getAccountHolderName().isEmpty()) {
            throw new InvalidAccountException("Account holder name is required");
        }
        
        // Validate based on account type
        if (account.getIban() != null && !account.getIban().isEmpty()) {
            if (!isValidIBAN(account.getIban())) {
                throw new InvalidAccountException("Invalid IBAN: " + account.getIban());
            }
        } else if (account.getAccountNumber() != null && !account.getAccountNumber().isEmpty()) {
            // Account number validation would be country-specific
            if (account.getAccountNumber().length() < 5) {
                throw new InvalidAccountException("Account number too short");
            }
        } else {
            throw new InvalidAccountException("Either IBAN or account number must be provided");
        }
    }
    
    /**
     * Gets the expected IBAN length for a country code.
     * 
     * @param countryCode The ISO 3166-1 alpha-2 country code
     * @return Expected length, or 0 if unknown
     */
    private static int getExpectedIBANLength(String countryCode) {
        return switch (countryCode) {
            case "AD" -> 24;
            case "AT", "BA", "EE", "LT", "XK" -> 20;
            case "AZ", "BR", "PS", "QA", "VA" -> 29;
            case "BE", "GL" -> 16;
            case "BG", "GB", "IE", "JO", "MC", "TN" -> 22;
            case "CH", "CR", "LI", "LV", "MZ" -> 21;
            case "CY", "KW", "MD", "PK", "SV", "TL" -> 28;
            case "CZ", "ES", "NO", "SE", "SK" -> 24;
            case "DE", "LU", "ME", "MK", "RS", "SA" -> 22;
            case "DK", "FI", "FO" -> 18;
            case "DO" -> 28;
            case "EG", "GE", "LC", "UA" -> 29;
            case "FR", "GI", "GR", "IT", "PT", "SM" -> 27;
            case "GT" -> 28;
            case "HR", "SI" -> 19;
            case "HU" -> 28;
            case "IL" -> 23;
            case "IS", "TR" -> 26;
            case "KZ" -> 20;
            case "MT" -> 31;
            case "MU" -> 30;
            case "NL" -> 18;
            case "PL" -> 28;
            case "RO" -> 24;
            default -> 0; // Unknown or variable length
        };
    }
    
    /**
     * Validates IBAN checksum using mod-97 algorithm.
     * 
     * @param iban The normalized IBAN
     * @return true if checksum is valid
     */
    private static boolean validateIBANChecksum(String iban) {
        // Move first 4 characters to end
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        
        // Replace letters with numbers (A=10, B=11, ..., Z=35)
        StringBuilder numericString = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numericString.append(c);
            } else {
                numericString.append(c - 'A' + 10);
            }
        }
        
        // Calculate mod-97
        BigDecimal number = new BigDecimal(numericString.toString());
        BigDecimal remainder = number.remainder(BigDecimal.valueOf(97));
        
        return remainder.intValue() == 1;
    }
}
