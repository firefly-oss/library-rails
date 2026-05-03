# Implementation Guide: Building a New Payment Rail

This guide provides step-by-step instructions for implementing a new payment rail (like SEPA) using the library-rails framework.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Step-by-Step Implementation](#step-by-step-implementation)
  - [Step 1: Create Rail-Specific Domain Models](#step-1-create-rail-specific-domain-models)
  - [Step 2: Implement Port Interfaces](#step-2-implement-port-interfaces)
  - [Step 3: Create Service Layer](#step-3-create-service-layer)
  - [Step 4: Create Controllers](#step-4-create-controllers)
  - [Step 5: Add Configuration](#step-5-add-configuration)
  - [Step 6: Testing](#step-6-testing)
- [Complete SEPA Example](#complete-sepa-example)
- [Advanced Features](#advanced-features)

---

## Overview

Implementing a new rail involves:
1. **Domain Models** (~200 lines) - Rail-specific data structures
2. **Port Implementations** (~1,200 lines) - Core business logic
3. **Service Layer** (~5 lines) - Extending AbstractRailService
4. **Controllers** (~30 lines) - Extending abstract controllers
5. **DTO Mappers** (~400 lines) - Request/response conversion
6. **Configuration** (~50 lines) - Spring Boot setup

**Total effort**: 4-8 days for a complete implementation

---

## Prerequisites

```xml
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>library-rails</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Required knowledge**:
- Spring Boot 3.x + WebFlux
- Project Reactor (Mono/Flux)
- Java 21
- Your target payment rail's API/specifications

---

## Step-by-Step Implementation

### Step 1: Create Rail-Specific Domain Models

Create domain models for rail-specific attributes in `domain/railspecific/`.

**Example: SEPA Credit Transfer**

```java path=/src/main/java/com/firefly/rails/impl/sepa/domain/SEPASpecific.java start=1
package com.firefly.rails.impl.sepa.domain;

import lombok.Builder;
import lombok.Data;

public class SEPASpecific {

    @Data
    @Builder
    public static class SEPACreditTransfer {
        /** SEPA Scheme (SCT, SCT Inst) */
        private SEPAScheme scheme;
        
        /** Purpose code (SEPA) */
        private String purposeCode;
        
        /** Creditor reference (structured) */
        private String creditorReference;
        
        /** Category purpose */
        private String categoryPurpose;
    }

    @Data
    @Builder
    public static class SEPADirectDebit {
        /** SEPA Scheme (SDD Core, SDD B2B) */
        private SEPAScheme scheme;
        
        /** Mandate reference */
        private String mandateReference;
        
        /** Mandate signature date */
        private String mandateSignatureDate;
        
        /** Sequence type (First, Recurring, One-off, Final) */
        private SequenceType sequenceType;
        
        /** Creditor ID */
        private String creditorId;
    }

    public enum SEPAScheme {
        SCT,         // SEPA Credit Transfer
        SCT_INST,    // SEPA Instant Credit Transfer
        SDD_CORE,    // SEPA Direct Debit Core
        SDD_B2B      // SEPA Direct Debit B2B
    }

    public enum SequenceType {
        FRST,  // First
        RCUR,  // Recurring
        OOFF,  // One-off
        FNAL   // Final
    }
}
```

---

### Step 2: Implement Port Interfaces

Implement all 10 port interfaces. Here's the key interface with SCA support:

#### PaymentRailPort Implementation

```java path=/src/main/java/com/firefly/rails/impl/sepa/ports/SEPAPaymentRailPort.java start=1
package com.firefly.rails.impl.sepa.ports;

import com.firefly.rails.adapter.ports.PaymentRailPort;
import com.firefly.rails.dtos.payments.*;
import com.firefly.rails.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SEPAPaymentRailPort implements PaymentRailPort {

    private final SEPAApiClient sepaApiClient;
    private final SEPAMapper mapper;

    // ==================== VALIDATION & SIMULATION ====================

    @Override
    public Mono<ResponseEntity<ValidationResponse>> validatePayment(ValidatePaymentRequest request) {
        log.info("Validating SEPA payment: IBAN={}", request.getCreditorAccount().getIban());
        
        return sepaApiClient.validateIban(request.getCreditorAccount().getIban())
            .flatMap(valid -> {
                if (!valid) {
                    return Mono.just(ResponseEntity.ok(ValidationResponse.builder()
                        .valid(false)
                        .errors(List.of("Invalid IBAN format"))
                        .build()));
                }
                
                // Additional validations
                return sepaApiClient.checkAccountExists(request.getCreditorAccount())
                    .map(exists -> ResponseEntity.ok(ValidationResponse.builder()
                        .valid(exists)
                        .warnings(exists ? List.of() : List.of("Account not found"))
                        .build()));
            });
    }

    @Override
    public Mono<ResponseEntity<SimulationResponse>> simulatePayment(SimulatePaymentRequest request) {
        log.info("Simulating SEPA payment: amount={}", request.getAmount());
        
        return sepaApiClient.calculateFees(request.getAmount(), request.getScheme())
            .map(fees -> ResponseEntity.ok(SimulationResponse.builder()
                .estimatedFees(fees)
                .estimatedSettlementDate(calculateSettlementDate(request.getScheme()))
                .exchangeRate(null) // Not applicable for SEPA (same currency)
                .build()));
    }

    // ==================== TWO-PHASE COMMIT WITH SCA ====================

    @Override
    public Mono<ResponseEntity<AuthorizationResponse>> authorizePayment(AuthorizePaymentRequest request) {
        log.info("Authorizing SEPA payment: amount={}", request.getAmount());
        
        return sepaApiClient.createAuthorization(mapper.toSepaAuthRequest(request))
            .flatMap(authResult -> {
                // Check if SCA is required
                if (authResult.isScaRequired() && !request.isRequestScaExemption()) {
                    // SCA challenge needed
                    return Mono.just(ResponseEntity.ok(AuthorizationResponse.builder()
                        .authorizationId(authResult.getAuthorizationId())
                        .status(PaymentStatus.PENDING_AUTHORIZATION)
                        .scaRequired(true)
                        .scaMethod(AuthenticationMethod.SMS_OTP)
                        .scaChallengeData(authResult.getScaChallengeData())
                        .expiresAt(authResult.getExpiresAt())
                        .build()));
                } else {
                    // No SCA required (exemption granted or not needed)
                    return Mono.just(ResponseEntity.ok(AuthorizationResponse.builder()
                        .authorizationId(authResult.getAuthorizationId())
                        .status(PaymentStatus.AUTHORIZED)
                        .scaRequired(false)
                        .scaExemptionApplied(request.isRequestScaExemption())
                        .build()));
                }
            });
    }

    @Override
    public Mono<ResponseEntity<AuthorizationResponse>> completeAuthentication(
            CompleteAuthenticationRequest request) {
        log.info("Completing SCA authentication: authId={}", request.getAuthorizationId());
        
        return sepaApiClient.verifyScaChallenge(
                request.getAuthorizationId(),
                request.getAuthenticationCode())
            .map(verified -> {
                if (verified) {
                    return ResponseEntity.ok(AuthorizationResponse.builder()
                        .authorizationId(request.getAuthorizationId())
                        .status(PaymentStatus.AUTHORIZED)
                        .scaRequired(false)
                        .scaCompleted(true)
                        .build());
                } else {
                    return ResponseEntity.ok(AuthorizationResponse.builder()
                        .authorizationId(request.getAuthorizationId())
                        .status(PaymentStatus.AUTHORIZATION_FAILED)
                        .scaRequired(true)
                        .scaCompleted(false)
                        .errorMessage("Invalid authentication code")
                        .build());
                }
            });
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> confirmPayment(String authorizationId) {
        log.info("Confirming SEPA payment: authId={}", authorizationId);
        
        return sepaApiClient.confirmAuthorization(authorizationId)
            .map(payment -> ResponseEntity.ok(mapper.toPaymentResponse(payment)));
    }

    @Override
    public Mono<ResponseEntity<CancellationResponse>> cancelAuthorization(String authorizationId) {
        log.info("Cancelling SEPA authorization: authId={}", authorizationId);
        
        return sepaApiClient.cancelAuthorization(authorizationId)
            .map(result -> ResponseEntity.ok(CancellationResponse.builder()
                .paymentId(authorizationId)
                .status(PaymentStatus.CANCELLED)
                .cancelledAt(result.getCancelledAt())
                .build()));
    }

    // ==================== DIRECT EXECUTION (SINGLE PHASE) ====================

    @Override
    public Mono<ResponseEntity<PaymentResponse>> initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating SEPA payment: amount={}, type={}", 
            request.getAmount(), request.getTransactionType());
        
        // Map to SEPA-specific request
        var sepaRequest = mapper.toSepaPaymentRequest(request);
        
        // Submit to SEPA network
        return sepaApiClient.submitPayment(sepaRequest)
            .map(response -> ResponseEntity.ok(mapper.toPaymentResponse(response)))
            .onErrorResume(error -> {
                log.error("SEPA payment failed", error);
                return Mono.just(ResponseEntity.ok(PaymentResponse.builder()
                    .status(PaymentStatus.FAILED)
                    .errorMessage(error.getMessage())
                    .build()));
            });
    }

    // ==================== QUERY OPERATIONS ====================

    @Override
    public Mono<ResponseEntity<PaymentResponse>> getPayment(String paymentId) {
        log.debug("Retrieving SEPA payment: {}", paymentId);
        
        return sepaApiClient.getPaymentById(paymentId)
            .map(payment -> ResponseEntity.ok(mapper.toPaymentResponse(payment)));
    }

    @Override
    public Mono<ResponseEntity<PaymentStatusResponse>> getPaymentStatus(String paymentReference) {
        log.debug("Getting SEPA payment status: {}", paymentReference);
        
        return sepaApiClient.getPaymentStatus(paymentReference)
            .map(status -> ResponseEntity.ok(PaymentStatusResponse.builder()
                .paymentReference(paymentReference)
                .status(status)
                .build()));
    }

    @Override
    public Mono<ResponseEntity<List<PaymentResponse>>> listPayments(ListPaymentsRequest request) {
        log.debug("Listing SEPA payments");
        
        return sepaApiClient.listPayments(
                request.getFromDate(),
                request.getToDate(),
                request.getStatus())
            .map(payments -> ResponseEntity.ok(
                payments.stream()
                    .map(mapper::toPaymentResponse)
                    .toList()));
    }

    // ==================== CANCELLATION & RETURNS ====================

    @Override
    public Mono<ResponseEntity<PaymentResponse>> cancelPayment(String paymentId) {
        log.info("Cancelling SEPA payment: {}", paymentId);
        
        return sepaApiClient.cancelPayment(paymentId)
            .map(result -> ResponseEntity.ok(mapper.toPaymentResponse(result)));
    }

    @Override
    public Mono<ResponseEntity<ReturnResponse>> requestReturn(ReturnRequest request) {
        log.info("Requesting SEPA return: paymentId={}", request.getPaymentId());
        
        return sepaApiClient.initiateReturn(
                request.getPaymentId(),
                request.getReason())
            .map(result -> ResponseEntity.ok(ReturnResponse.builder()
                .returnId(result.getReturnId())
                .originalPaymentId(request.getPaymentId())
                .status(result.getStatus())
                .build()));
    }

    // Helper methods
    private LocalDate calculateSettlementDate(SEPAScheme scheme) {
        return switch (scheme) {
            case SCT_INST -> LocalDate.now(); // Instant
            case SCT -> LocalDate.now().plusDays(1); // Next business day
            case SDD_CORE, SDD_B2B -> LocalDate.now().plusDays(2); // D+2
        };
    }
}
```

**Key Points**:
- ✅ **SCA Support**: `authorizePayment()` checks if SCA is required
- ✅ **Two-Phase Commit**: Authorize → Complete Auth → Confirm
- ✅ **Single-Phase**: `initiatePayment()` for simple flows
- ✅ **Reactive**: All methods return `Mono<ResponseEntity<T>>`

---

### Step 3: Create Service Layer

Extend the abstract service layer (minimal boilerplate):

```java path=/src/main/java/com/firefly/rails/impl/sepa/service/SEPARailService.java start=1
package com.firefly.rails.impl.sepa.service;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.service.AbstractRailService;
import org.springframework.stereotype.Service;

@Service
public class SEPARailService extends AbstractRailService {
    
    public SEPARailService(RailAdapter railAdapter) {
        super(railAdapter);
    }
}
```

That's it! All logging, error handling, and common operations are inherited.

---

### Step 4: Create Controllers

Extend the abstract controllers to expose REST APIs:

```java path=/src/main/java/com/firefly/rails/impl/sepa/controller/SEPAPaymentController.java start=1
package com.firefly.rails.impl.sepa.controller;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.adapter.web.AbstractPaymentRailController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sepa")
public class SEPAPaymentController extends AbstractPaymentRailController {
    
    public SEPAPaymentController(RailAdapter railAdapter) {
        super(railAdapter);
    }
}
```

This gives you **13 endpoints** automatically:
- `POST /api/sepa/payments/validate`
- `POST /api/sepa/payments/simulate`
- `POST /api/sepa/payments/authorize` (Phase 1)
- `POST /api/sepa/payments/authenticate` (SCA challenge)
- `POST /api/sepa/payments/confirm/{authId}` (Phase 2)
- `DELETE /api/sepa/payments/authorize/{authId}`
- `POST /api/sepa/payments` (direct execution)
- `GET /api/sepa/payments/{id}`
- `GET /api/sepa/payments/status/{ref}`
- `GET /api/sepa/payments`
- `DELETE /api/sepa/payments/{id}`
- `POST /api/sepa/payments/return`

Similarly, create controllers for other ports:

```java path=/src/main/java/com/firefly/rails/impl/sepa/controller/SEPAMandateController.java start=1
@RestController
@RequestMapping("/api/sepa")
public class SEPAMandateController extends AbstractMandateController {
    public SEPAMandateController(RailAdapter railAdapter) {
        super(railAdapter);
    }
}
```

```java path=/src/main/java/com/firefly/rails/impl/sepa/controller/SEPABulkPaymentController.java start=1
@RestController
@RequestMapping("/api/sepa")
public class SEPABulkPaymentController extends AbstractBulkPaymentController {
    public SEPABulkPaymentController(RailAdapter railAdapter) {
        super(railAdapter);
    }
}
```

---

### Step 5: Add Configuration

Create Spring Boot auto-configuration:

```java path=/src/main/java/com/firefly/rails/impl/sepa/config/SEPAAutoConfiguration.java start=1
package com.firefly.rails.impl.sepa.config;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.impl.sepa.adapter.SEPARailAdapter;
import com.firefly.rails.impl.sepa.client.SEPAApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SEPAProperties.class)
@ConditionalOnProperty(name = "firefly.rail.rail-type", havingValue = "sepa")
public class SEPAAutoConfiguration {

    @Bean
    public SEPAApiClient sepaApiClient(SEPAProperties properties) {
        return new SEPAApiClient(properties);
    }

    @Bean
    public RailAdapter railAdapter(
            SEPAPaymentRailPort paymentPort,
            SEPAMandatePort mandatePort,
            SEPABulkPaymentPort bulkPaymentPort,
            SEPASettlementPort settlementPort,
            SEPAStatusPort statusPort,
            SEPAReconciliationPort reconciliationPort,
            SEPARailSpecificPort railSpecificPort) {
        
        return new SEPARailAdapter(
            paymentPort,
            mandatePort,
            bulkPaymentPort,
            settlementPort,
            statusPort,
            reconciliationPort,
            railSpecificPort
        );
    }
}
```

**Configuration Properties**:

```java path=/src/main/java/com/firefly/rails/impl/sepa/config/SEPAProperties.java start=1
package com.firefly.rails.impl.sepa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "firefly.rail.sepa")
public class SEPAProperties {
    
    /** SEPA API base URL */
    private String apiUrl;
    
    /** API credentials */
    private String clientId;
    private String clientSecret;
    
    /** Timeout settings */
    private int connectTimeout = 5000;
    private int readTimeout = 30000;
    
    /** Default scheme */
    private String defaultScheme = "SCT";
    
    /** Enable SCA by default */
    private boolean scaEnabled = true;
}
```

**application.yml**:

```yaml path=null start=null
firefly:
  rail:
    rail-type: sepa
    base-path: /api/sepa
    
    sepa:
      api-url: https://api.sepa-provider.com
      client-id: ${SEPA_CLIENT_ID}
      client-secret: ${SEPA_CLIENT_SECRET}
      default-scheme: SCT
      sca-enabled: true
```

---

### Step 6: Testing

Create comprehensive tests for your implementation:

```java path=/src/test/java/com/firefly/rails/impl/sepa/SEPAPaymentRailPortTest.java start=1
package com.firefly.rails.impl.sepa;

import com.firefly.rails.dtos.payments.*;
import com.firefly.rails.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SEPAPaymentRailPortTest {

    @Autowired
    private SEPAPaymentRailPort paymentPort;

    @Test
    void testAuthorizePaymentWithSCA() {
        // Given
        var request = AuthorizePaymentRequest.builder()
            .amount(new Money("100.00", Currency.EUR))
            .debtorAccount(BankAccount.fromIban("John Doe", "DE89370400440532013000", "COBADEFFXXX"))
            .creditorAccount(BankAccount.fromIban("Jane Smith", "FR1420041010050500013M02606", "BNPAFRPPXXX"))
            .transactionType(TransactionType.CREDIT_TRANSFER)
            .authenticationContext(AuthenticationContext.withOtp("token123", "https://callback.com"))
            .build();

        // When & Then
        StepVerifier.create(paymentPort.authorizePayment(request))
            .assertNext(response -> {
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                var body = response.getBody();
                assertThat(body).isNotNull();
                assertThat(body.isScaRequired()).isTrue();
                assertThat(body.getScaMethod()).isEqualTo(AuthenticationMethod.SMS_OTP);
                assertThat(body.getAuthorizationId()).isNotEmpty();
            })
            .verifyComplete();
    }

    @Test
    void testCompleteAuthenticationAndConfirm() {
        // 1. Authorize
        var authRequest = AuthorizePaymentRequest.builder()
            .amount(new Money("50.00", Currency.EUR))
            .debtorAccount(BankAccount.fromIban("Test User", "DE89370400440532013000", "COBADEFFXXX"))
            .creditorAccount(BankAccount.fromIban("Merchant", "FR1420041010050500013M02606", "BNPAFRPPXXX"))
            .transactionType(TransactionType.CREDIT_TRANSFER)
            .build();

        String authorizationId = paymentPort.authorizePayment(authRequest)
            .map(response -> response.getBody().getAuthorizationId())
            .block();

        // 2. Complete authentication
        var completeAuthRequest = CompleteAuthenticationRequest.builder()
            .authorizationId(authorizationId)
            .authenticationCode("123456")
            .build();

        StepVerifier.create(paymentPort.completeAuthentication(completeAuthRequest))
            .assertNext(response -> {
                assertThat(response.getBody().isScaCompleted()).isTrue();
                assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
            })
            .verifyComplete();

        // 3. Confirm payment
        StepVerifier.create(paymentPort.confirmPayment(authorizationId))
            .assertNext(response -> {
                assertThat(response.getBody().getStatus()).isIn(
                    PaymentStatus.PENDING, PaymentStatus.PROCESSING
                );
            })
            .verifyComplete();
    }
}
```

---

## Complete SEPA Example

### SEPA Credit Transfer Flow

```java path=null start=null
@Service
public class SEPAPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Single-phase SEPA Credit Transfer (no SCA)
     */
    public Mono<PaymentResponse> sendSEPAPayment(
            String debtorIban,
            String creditorIban,
            BigDecimal amount,
            String reference) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .debtorAccount(BankAccount.fromIban("John Doe", debtorIban, "COBADEFFXXX"))
                .creditorAccount(BankAccount.fromIban("Jane Smith", creditorIban, "BNPAFRPPXXX"))
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .endToEndReference(reference)
                .remittanceInformation("Invoice payment")
                .settlementSpeed(SettlementSpeed.STANDARD)
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Two-phase SEPA payment with SCA
     */
    public Mono<PaymentWithSCA> sendSEPAPaymentWithSCA(
            String debtorIban,
            String creditorIban,
            BigDecimal amount,
            String reference) {
        
        // Phase 1: Authorize
        return railAdapter.payments()
            .authorizePayment(AuthorizePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .debtorAccount(BankAccount.fromIban("John Doe", debtorIban, "COBADEFFXXX"))
                .creditorAccount(BankAccount.fromIban("Jane Smith", creditorIban, "BNPAFRPPXXX"))
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .endToEndReference(reference)
                .authenticationContext(AuthenticationContext.withOtp(
                    "challenge-token",
                    "https://myapp.com/callback"))
                .build())
            .map(response -> {
                var authResponse = response.getBody();
                
                if (authResponse.isScaRequired()) {
                    // Return SCA challenge to user
                    return new PaymentWithSCA(
                        authResponse.getAuthorizationId(),
                        authResponse.getScaChallengeData(),
                        authResponse.getScaMethod()
                    );
                } else {
                    // No SCA needed, auto-confirm
                    railAdapter.payments()
                        .confirmPayment(authResponse.getAuthorizationId())
                        .subscribe();
                    
                    return new PaymentWithSCA(
                        authResponse.getAuthorizationId(),
                        null,
                        null
                    );
                }
            });
    }

    /**
     * Complete SCA and confirm payment
     */
    public Mono<PaymentResponse> completeScaAndConfirm(
            String authorizationId,
            String authCode) {
        
        // Complete SCA challenge
        return railAdapter.payments()
            .completeAuthentication(CompleteAuthenticationRequest.builder()
                .authorizationId(authorizationId)
                .authenticationCode(authCode)
                .build())
            .flatMap(authResponse -> {
                if (authResponse.getBody().isScaCompleted()) {
                    // SCA successful, confirm payment
                    return railAdapter.payments()
                        .confirmPayment(authorizationId)
                        .map(ResponseEntity::getBody);
                } else {
                    // SCA failed
                    return Mono.error(new RuntimeException("SCA authentication failed"));
                }
            });
    }
}
```

### SEPA Direct Debit Flow

```java path=null start=null
@Service
public class SEPADirectDebitService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Create SEPA Direct Debit mandate
     */
    public Mono<MandateResponse> createMandate(
            String debtorIban,
            String creditorId) {
        
        return railAdapter.mandates()
            .createMandate(CreateMandateRequest.builder()
                .debtorAccount(BankAccount.fromIban(
                    "Customer Name",
                    debtorIban,
                    "COBADEFFXXX"))
                .creditorId(creditorId)
                .mandateType(MandateType.RECURRING)
                .maxAmount(new Money("1000.00", Currency.EUR))
                .railSpecificData(Map.of(
                    "scheme", "SDD_CORE",
                    "sequenceType", "FRST"
                ))
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Execute direct debit payment using mandate
     */
    public Mono<PaymentResponse> executeDirectDebit(
            String mandateReference,
            BigDecimal amount,
            String reference) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .transactionType(TransactionType.DIRECT_DEBIT)
                .endToEndReference(reference)
                .remittanceInformation("Direct debit payment")
                .metadata(Map.of(
                    "mandateReference", mandateReference,
                    "sequenceType", "RCUR"
                ))
                .build())
            .map(ResponseEntity::getBody);
    }
}
```

### SEPA Instant Credit Transfer (SCT Inst)

```java path=null start=null
@Service
public class SEPAInstantPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Send instant SEPA payment (real-time settlement)
     */
    public Mono<PaymentResponse> sendInstantPayment(
            String debtorIban,
            String creditorIban,
            BigDecimal amount) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .debtorAccount(BankAccount.fromIban("John Doe", debtorIban, "COBADEFFXXX"))
                .creditorAccount(BankAccount.fromIban("Jane Smith", creditorIban, "BNPAFRPPXXX"))
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .settlementSpeed(SettlementSpeed.INSTANT)
                .metadata(Map.of("scheme", "SCT_INST"))
                .build())
            .map(ResponseEntity::getBody)
            .timeout(Duration.ofSeconds(10)) // Instant payments have strict timeouts
            .onErrorResume(TimeoutException.class, e -> {
                log.error("Instant payment timeout - falling back to standard");
                // Fallback to standard SEPA if instant fails
                return sendStandardPayment(debtorIban, creditorIban, amount);
            });
    }
}
```

---

## Advanced Features

### 1. Strong Customer Authentication (SCA) Flow

**Complete SCA Flow Diagram**:

```
┌──────────────┐
│  Client App  │
└──────┬───────┘
       │
       │ 1. POST /payments/authorize
       │    (with payment details)
       ▼
┌──────────────────────┐
│  Payment Controller  │
└──────┬───────────────┘
       │
       │ 2. Check SCA requirement
       ▼
┌────────────────────────┐
│  SEPA Payment Port     │  ◄─── Determines if SCA needed
└──────┬─────────────────┘        based on:
       │                          - Amount threshold (€30)
       │                          - Transaction risk score
       │                          - Merchant category
       │                          - User authentication history
       │
       │ 3a. SCA Required
       │
       ▼
┌──────────────────────────┐
│ Return Authorization     │
│ + SCA Challenge          │
│   - authorizationId      │
│   - scaChallengeData     │
│   - scaMethod (SMS_OTP)  │
└──────┬───────────────────┘
       │
       │ 4. Send OTP to user's phone
       │
       ▼
┌──────────────┐
│  User Device │ ◄─── Receives SMS with code
└──────┬───────┘
       │
       │ 5. User enters OTP code
       │
       │ 6. POST /payments/authenticate
       │    { authorizationId, authenticationCode }
       ▼
┌──────────────────────┐
│  Complete Auth       │
└──────┬───────────────┘
       │
       │ 7. Verify OTP code
       │
       ▼
┌────────────────────────┐
│ Authorization Updated  │
│ status: AUTHORIZED     │
│ scaCompleted: true     │
└──────┬─────────────────┘
       │
       │ 8. POST /payments/confirm/{authorizationId}
       │
       ▼
┌────────────────────────┐
│ Payment Executed       │
│ status: PROCESSING     │
└────────────────────────┘
```

**SCA Implementation Tips**:
- ✅ Always check `isScaRequired()` in authorization response
- ✅ Store `authorizationId` for later confirmation
- ✅ Set appropriate timeout for SCA challenge (5-10 minutes)
- ✅ Handle SCA exemptions (low-value transactions, trusted beneficiaries)
- ✅ Support multiple SCA methods (SMS, biometric, hardware token)

### 2. Bulk Payment Processing

```java path=null start=null
@Service
public class BulkPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Submit bulk SEPA credit transfers
     */
    public Mono<BulkPaymentResponse> submitBulkPayments(List<PaymentDetail> payments) {
        
        var bulkRequest = BulkPaymentRequest.builder()
            .payments(payments.stream()
                .map(p -> InitiatePaymentRequest.builder()
                    .amount(new Money(p.getAmount(), Currency.EUR))
                    .debtorAccount(p.getDebtorAccount())
                    .creditorAccount(p.getCreditorAccount())
                    .transactionType(TransactionType.CREDIT_TRANSFER)
                    .endToEndReference(p.getReference())
                    .build())
                .toList())
            .requestedExecutionDate(LocalDate.now().plusDays(1))
            .build();

        return railAdapter.bulkPayments()
            .submitBulkPayment(bulkRequest)
            .map(ResponseEntity::getBody);
    }

    /**
     * Track bulk payment status
     */
    public Mono<BulkPaymentStatusResponse> trackBulkPayment(String bulkPaymentId) {
        return railAdapter.bulkPayments()
            .getBulkPaymentStatus(bulkPaymentId)
            .map(ResponseEntity::getBody);
    }
}
```

### 3. Reconciliation

```java path=null start=null
@Service
public class ReconciliationService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Reconcile payments against bank statements
     */
    public Mono<ReconciliationResponse> reconcilePayments(
            LocalDate from,
            LocalDate to,
            List<Transaction> expectedTransactions) {
        
        return railAdapter.reconciliation()
            .reconcile(ReconciliationRequest.builder()
                .fromDate(from)
                .toDate(to)
                .expectedTransactions(expectedTransactions)
                .build())
            .map(ResponseEntity::getBody)
            .doOnNext(result -> {
                if (!result.getDiscrepancies().isEmpty()) {
                    log.warn("Found {} discrepancies in reconciliation",
                        result.getDiscrepancies().size());
                }
            });
    }
}
```

### 4. Settlement Reporting

```java path=null start=null
@Service
public class SettlementService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Get daily settlement report
     */
    public Mono<SettlementReportResponse> getDailySettlement(LocalDate date) {
        
        return railAdapter.settlement()
            .getSettlementReport(SettlementReportRequest.builder()
                .reportDate(date)
                .reportType(ReportType.DAILY)
                .includeDetailedTransactions(true)
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Track real-time settlement
     */
    public Flux<SettlementEvent> streamSettlementEvents() {
        return railAdapter.settlement()
            .streamSettlementEvents(SettlementStreamRequest.builder()
                .fromTimestamp(Instant.now())
                .build());
    }
}
```

---

## Summary

**Implementation Checklist**:

- ✅ Create rail-specific domain models
- ✅ Implement all 10 port interfaces
- ✅ Add SCA support in `authorizePayment()` and `completeAuthentication()`
- ✅ Extend `AbstractRailService`
- ✅ Extend abstract controllers (Payment, Mandate, Bulk, etc.)
- ✅ Create DTO mappers
- ✅ Add Spring Boot auto-configuration
- ✅ Write comprehensive tests
- ✅ Document rail-specific behaviors

**Time Estimate**: 4-8 days for complete implementation

**Result**: Production-ready payment rail with 40+ REST endpoints, full SCA support, reactive processing, and enterprise-grade resilience!

---

## Next Steps

1. Review [ARCHITECTURE.md](ARCHITECTURE.md) for hexagonal architecture details
2. Check [TESTING_GUIDE.md](TESTING_GUIDE.md) for testing strategies
3. See [USAGE_GUIDE.md](USAGE_GUIDE.md) for common usage patterns
4. Explore [RAIL_COVERAGE.md](../RAIL_COVERAGE.md) for supported rails

---

**License**: Apache 2.0  
**Copyright**: 2025 Firefly Software Foundation
