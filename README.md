# Firefly Banking Rails Library

A comprehensive Banking Payment Rails abstraction library for the Firefly platform, built on hexagonal architecture principles.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)

## Overview

The `library-rails` library provides a unified, type-safe interface for integrating with multiple Banking Payment Rails (ACH, SWIFT, SEPA, FPS, RTP, etc.) while maintaining clean architecture and rail independence.

### Supported Banking Rails

#### Traditional Banking Rails
- **ACH** - Automated Clearing House (US domestic batch transfers)
- **SWIFT** - International wire transfers
- **SEPA** - Single Euro Payments Area (European credit transfers & direct debits)
- **CHIPS** - Clearing House Interbank Payments System (US large-value)
- **FPS** - Faster Payments Service (UK real-time)
- **Fedwire** - Federal Reserve Wire Network (US real-time gross settlement)
- **TARGET2** - Trans-European Automated Real-time Gross settlement
- **BACS** - UK Bankers' Automated Clearing Services
- **Interac** - Canadian electronic funds transfer

#### Modern/Real-Time Rails
- **RTP** - Real-Time Payments (US instant payment network)
- **PIX** - Brazilian instant payment system
- **UPI** - Unified Payments Interface (India)
- **Zelle** - US peer-to-peer payment network

#### Alternative Payment Methods
- **Card Networks** - Visa, Mastercard, AmEx payment processing
- **Mobile Wallets** - Apple Pay, Google Pay, Samsung Pay
- **Crypto/Blockchain** - Bitcoin, Ethereum, stablecoins

### Key Features

#### **Core Capabilities**
- **9 Port Interfaces** (47+ methods) - Complete rail operation coverage
- **Payment Initiation** - Credit transfers, debits, wire transfers (12 methods)
- **Scheduled & Recurring Payments** - Future-dated and standing orders (9 methods)
- **Direct Debit Management** - Mandate creation and management (5 methods)
- **Bulk/Batch Payments** - High-volume payment processing (3 methods)
- **Settlement Reporting** - Real-time settlement tracking (4 methods)
- **Status Inquiry** - Real-time payment tracking (3 methods)
- **Reconciliation** - Transaction matching and discrepancy detection (3 methods)
- **Compliance & Regulatory** - AML/KYC/Sanctions screening (6 methods)
- **Rail-Specific Operations** - Custom rail features (2 methods)
- **9 Auto-Configured Controllers** - Zero boilerplate REST API (43+ endpoints)
- **Abstract Service Layer** - Standardized implementation patterns
- **Reactive & Non-Blocking** - Built on Project Reactor
- **Type-Safe** - Strong typing throughout

#### **Enterprise-Grade Resilience**
- **Circuit Breaker** - Prevents cascading failures (Resilience4j)
- **Rate Limiter** - Protects against rail quota exhaustion
- **Retry with Backoff** - Automatic exponential retry
- **Bulkhead** - Limits concurrent calls per rail
- **Timeout Protection** - Time limiter for all operations
- **Health Checks** - Spring Actuator integration
- **Metrics** - Prometheus/Micrometer observability

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>library-rails</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Use in Your Application

```java
@Service
public class PaymentService {
    
    @Autowired
    private RailAdapter railAdapter;
    
    public Mono<PaymentResponse> initiatePayment(PaymentRequest request) {
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(request.getAmount(), Currency.USD))
                .debtorAccount(request.getDebtorAccount())
                .creditorAccount(request.getCreditorAccount())
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .build())
            .map(ResponseEntity::getBody);
    }
}
```

### 3. Configuration

```yaml
firefly:
  rail:
    rail-type: ach  # or swift, sepa, fps, rtp, etc.
    base-path: /api/rails
```

## Core Architecture

### Port Interfaces (Hexagonal Architecture)

The library defines **9 port interfaces** representing all rail operations:

| Port | Purpose | Methods |
|------|---------|---------|
| **PaymentRailPort** | Payment initiation & management | 12 |
| **ScheduledPaymentPort** | Scheduled & recurring payments | 9 |
| **CompliancePort** | AML/KYC/Sanctions screening | 6 |
| **MandatePort** | Direct debit mandates | 5 |
| **SettlementPort** | Settlement reporting | 4 |
| **StatusPort** | Real-time status tracking | 3 |
| **BulkPaymentPort** | Batch payments | 3 |
| **ReconciliationPort** | Transaction reconciliation | 3 |
| **RailSpecificPort** | Custom rail operations | 2 |
| **RailAdapter** | Main entry point | All 9 ports (47 methods) |

### Abstract Components

Implementations inherit pre-built functionality:

#### **Service Layer**
- **AbstractRailService** - Service layer with logging and error handling

#### **Controller Layer (Zero Boilerplate REST APIs)**
- **AbstractPaymentRailController** - Payment operations with SCA support (12 endpoints)
- **AbstractScheduledPaymentController** - Scheduled & recurring payments (9 endpoints)
- **AbstractComplianceController** - AML/KYC compliance (6 endpoints)
- **AbstractMandateController** - Direct debit mandates (5 endpoints)
- **AbstractSettlementController** - Settlement reporting (4 endpoints)
- **AbstractStatusController** - Status inquiry (3 endpoints)
- **AbstractBulkPaymentController** - Bulk payments (3 endpoints)
- **AbstractReconciliationController** - Reconciliation (3 endpoints)
- **AbstractRailSpecificController** - Custom rail operations (2 endpoints)

**Total: 9 abstract controllers providing 47+ REST endpoints**

## Usage Examples

### ACH Payment (US)

```java
@PostMapping("/ach-payment")
public Mono<PaymentResponse> sendACH(@RequestBody ACHRequest req) {
    return railAdapter.payments()
        .initiatePayment(InitiatePaymentRequest.builder()
            .amount(new Money(req.getAmount(), Currency.USD))
            .debtorAccount(BankAccount.fromAccountNumber(
                req.getDebtorName(), 
                req.getAccountNumber(), 
                req.getRoutingNumber()))
            .creditorAccount(BankAccount.fromAccountNumber(
                req.getCreditorName(),
                req.getBeneficiaryAccount(),
                req.getBeneficiaryRouting()))
            .transactionType(TransactionType.CREDIT_TRANSFER)
            .settlementSpeed(SettlementSpeed.STANDARD)
            .build())
        .map(ResponseEntity::getBody);
}
```

### SEPA Credit Transfer (Europe)

```java
@PostMapping("/sepa-payment")
public Mono<PaymentResponse> sendSEPA(@RequestBody SEPARequest req) {
    return railAdapter.payments()
        .initiatePayment(InitiatePaymentRequest.builder()
            .amount(new Money(req.getAmount(), Currency.EUR))
            .debtorAccount(BankAccount.fromIban(
                req.getDebtorName(), 
                req.getDebtorIban(), 
                req.getDebtorBIC()))
            .creditorAccount(BankAccount.fromIban(
                req.getCreditorName(),
                req.getCreditorIban(),
                req.getCreditorBIC()))
            .transactionType(TransactionType.CREDIT_TRANSFER)
            .endToEndReference(req.getReference())
            .remittanceInformation(req.getDescription())
            .build())
        .map(ResponseEntity::getBody);
}
```

### Real-Time Payment (RTP/FPS)

```java
@PostMapping("/instant-payment")
public Mono<PaymentResponse> sendInstant(@RequestBody InstantPaymentRequest req) {
    return railAdapter.payments()
        .initiatePayment(InitiatePaymentRequest.builder()
            .amount(req.getMoney())
            .debtorAccount(req.getDebtorAccount())
            .creditorAccount(req.getCreditorAccount())
            .transactionType(TransactionType.CREDIT_TRANSFER)
            .settlementSpeed(SettlementSpeed.INSTANT)
            .build())
        .map(ResponseEntity::getBody);
}
```

### Payment with Strong Customer Authentication (SCA)

```java
@PostMapping("/payment-with-sca")
public Mono<AuthorizationResponse> initiateSecurePayment(@RequestBody PaymentRequest req) {
    // Phase 1: Authorize payment (triggers SCA if needed)
    return railAdapter.payments()
        .authorizePayment(AuthorizePaymentRequest.builder()
            .amount(new Money(req.getAmount(), Currency.EUR))
            .debtorAccount(req.getDebtorAccount())
            .creditorAccount(req.getCreditorAccount())
            .transactionType(TransactionType.CREDIT_TRANSFER)
            .authenticationContext(AuthenticationContext.withOtp(
                "challenge-token",
                "https://myapp.com/callback"))
            .build())
        .map(ResponseEntity::getBody)
        .doOnNext(response -> {
            if (response.isScaRequired()) {
                // Store authorizationId and prompt user for OTP
                log.info("SCA required. Authorization ID: {}", response.getAuthorizationId());
                log.info("OTP sent via: {}", response.getScaMethod());
            }
        });
}

@PostMapping("/complete-sca")
public Mono<PaymentResponse> completeScaAndConfirm(
        @RequestParam String authorizationId,
        @RequestParam String otpCode) {
    
    // Complete SCA authentication
    return railAdapter.payments()
        .completeAuthentication(CompleteAuthenticationRequest.builder()
            .authorizationId(authorizationId)
            .authenticationCode(otpCode)
            .build())
        .flatMap(authResponse -> {
            if (authResponse.getBody().isScaCompleted()) {
                // Phase 2: Confirm and execute payment
                return railAdapter.payments()
                    .confirmPayment(authorizationId)
                    .map(ResponseEntity::getBody);
            } else {
                return Mono.error(new RuntimeException("Authentication failed"));
            }
        });
}
```

### Bulk Payment Submission

```java
@PostMapping("/bulk-payments")
public Mono<BulkPaymentResponse> submitBulkPayment(@RequestBody BulkRequest req) {
    return railAdapter.bulkPayments()
        .submitBulkPayment(BulkPaymentRequest.builder()
            .payments(req.getPayments())
            .build())
        .map(ResponseEntity::getBody);
}
```

### Direct Debit Mandate (SEPA)

```java
@PostMapping("/mandates")
public Mono<MandateResponse> createMandate(@RequestBody MandateRequest req) {
    return railAdapter.mandates()
        .createMandate(CreateMandateRequest.builder()
            .debtorAccount(req.getDebtorAccount())
            .creditorId(req.getCreditorId())
            .build())
        .map(ResponseEntity::getBody);
}
```

## Domain Models

Type-safe value objects for banking operations:

- **Money** - Monetary amounts with currency (immutable)
- **Currency** - ISO 4217 currency codes (40+ supported) + cryptocurrencies
- **BankAccount** - IBAN, account/routing, SWIFT codes
- **RailType** - Supported payment rails (16 types)
- **PaymentStatus** - Payment lifecycle states (14 states)
- **TransactionType** - Transaction types (14 types)
- **SettlementSpeed** - Settlement timing (5 speeds)

## Supported Flows

✅ Credit transfers (push payments)  
✅ Debit transfers (pull payments)  
✅ Direct debits with mandates  
✅ Wire transfers (SWIFT, Fedwire)  
✅ Real-time/instant payments  
✅ Bulk/batch payments  
✅ Standing orders  
✅ Settlement reporting  
✅ Payment tracking & status  
✅ Reconciliation  
✅ Rail-specific features (extensible)  
✅ **Strong Customer Authentication (SCA)** - PSD2 compliant two-factor authentication  
✅ **Two-Phase Commit** - Authorize, authenticate, then confirm payments

### Payment Execution Flows

#### 1. Single-Phase Flow (Simple Payments)

```
Client → POST /payments → Rail → Response (Payment Created)
```

Use for:
- Low-value transactions
- Pre-authorized payments
- No SCA required

#### 2. Two-Phase Flow with SCA (Secure Payments)

```
Phase 1: Authorization
Client → POST /payments/authorize → Rail
       ← Authorization + SCA Challenge

SCA Authentication:
Client → POST /payments/authenticate (with OTP/code)
       ← Authorization Confirmed

Phase 2: Confirmation
Client → POST /payments/confirm/{authId}
       ← Payment Executed
```

Use for:
- High-value transactions (>€30 for SEPA)
- First-time beneficiaries
- Regulatory compliance (PSD2/SCA)

#### 3. Direct Debit Flow with Mandates

```
Setup Phase:
Client → POST /mandates (create mandate)
       ← Mandate ID

Execution Phase:
Client → POST /payments (with mandate reference)
       ← Payment Executed
```

Use for:
- Recurring payments
- Subscription billing
- Utility payments

## Implementation

To add a new banking rail implementation:

1. Create a new module (e.g., `library-rails-ach-impl`)
2. Implement the 9 port interfaces (47 methods, ~1,500 lines of rail-specific logic)
3. Extend `AbstractRailService` (~5 lines)
4. Extend 9 abstract controllers (~50 lines total)
5. Create mappers for DTO conversion (~500 lines)
6. Add Spring Boot auto-configuration (~50 lines)

**Result**: Complete REST API with **47+ endpoints**, service layer, logging, error handling, and enterprise features!

**Implementation effort**: 5-10 days → **Productivity multiplier: 7x**

## Benefits

### For Implementations
- **Zero boilerplate** - Inherit service layer and controllers
- **Standardized patterns** - Consistent across all rails
- **Built-in logging** - Automatic operation logging
- **Error handling** - Exception mapping included
- **Type-safe** - Strong typing prevents errors

### For Applications
- **Rail independence** - Switch rails via configuration
- **Consistent API** - Same interface for all rails
- **Multi-rail** - Support multiple rails simultaneously
- **Extensible** - Add custom operations without breaking standards
- **Production-ready** - Logging, validation, reactive support

## Technical Details

- **Artifact**: `library-rails`
- **Java**: 21
- **Framework**: Spring Boot 3.x + WebFlux
- **Architecture**: Hexagonal (Ports & Adapters)
- **Reactive**: Project Reactor (Mono/Flux)
- **Build Tool**: Maven

## Statistics

- **9 port interfaces** (47 methods total)
  - PaymentRailPort: 12 methods
  - ScheduledPaymentPort: 9 methods
  - CompliancePort: 6 methods
  - MandatePort: 5 methods
  - SettlementPort: 4 methods
  - StatusPort: 3 methods
  - BulkPaymentPort: 3 methods
  - ReconciliationPort: 3 methods
  - RailSpecificPort: 2 methods
- **1 adapter interface** (RailAdapter - main entry point)
- **28 domain models** - Type-safe value objects (Money, Currency, BankAccount, etc.)
- **60 DTOs** - Complete request/response objects
- **9 exception classes** - Comprehensive error handling
- **9 abstract controllers** - Zero boilerplate REST APIs (47+ endpoints)
- **1 abstract service layer** - Standardized implementation patterns
- **1 health indicator** - Spring Actuator integration
- **129 Java files** (~6,200 lines of production code)

## License

Copyright 2025 Firefly Software Foundation

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

## Documentation

For comprehensive documentation, see the [docs/](docs/) folder:

- **[Complete Documentation Index](docs/README.md)** - Start here
- **[Usage Guide](docs/USAGE_GUIDE.md)** - ✨ **NEW** Common payment scenarios (SEPA, SWIFT, SCA, etc.)
- **[Implementation Guide](docs/IMPLEMENTATION_GUIDE.md)** - ✨ **NEW** Step-by-step guide to building new rails
- **[Architecture Guide](docs/ARCHITECTURE.md)** - Hexagonal architecture and design patterns
- **[Testing Guide](docs/TESTING_GUIDE.md)** - Comprehensive testing strategies
- **[Complete Features](docs/ULTIMATE_FEATURES.md)** - All 54+ features with examples
- **[Executive Summary](docs/EXECUTIVE_SUMMARY.md)** - Business value and ROI

## Support

For questions or issues:
- **Documentation**: See [docs/README.md](docs/README.md) for complete index
- **Architecture**: Review [hexagonal design patterns](docs/ARCHITECTURE.md)
