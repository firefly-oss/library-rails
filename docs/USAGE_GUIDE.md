# Usage Guide: Common Payment Scenarios

This guide demonstrates common usage patterns for the library-rails library with practical examples.

## Table of Contents

- [Getting Started](#getting-started)
- [Payment Scenarios](#payment-scenarios)
  - [Simple Credit Transfer](#simple-credit-transfer)
  - [Payment with SCA (Two-Phase Commit)](#payment-with-sca-two-phase-commit)
  - [SEPA Credit Transfer](#sepa-credit-transfer)
  - [SEPA Direct Debit](#sepa-direct-debit)
  - [SWIFT International Transfer](#swift-international-transfer)
  - [Instant/Real-Time Payments](#instantreal-time-payments)
- [Bulk Operations](#bulk-operations)
- [Mandate Management](#mandate-management)
- [Payment Tracking](#payment-tracking)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)

---

## Getting Started

### Add Dependency

```xml path=null start=null
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>library-rails</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Configure Rail

```yaml path=null start=null
firefly:
  rail:
    rail-type: sepa  # or ach, swift, fps, rtp, etc.
    base-path: /api/rails
```

### Inject RailAdapter

```java path=null start=null
@Service
public class PaymentService {
    
    @Autowired
    private RailAdapter railAdapter;
    
    // Use railAdapter for all payment operations
}
```

---

## Payment Scenarios

### Simple Credit Transfer

**Use case**: Basic payment without SCA (low-value, pre-authorized)

```java path=null start=null
@Service
public class SimplePaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Send a simple payment (single-phase execution)
     */
    public Mono<PaymentResponse> sendPayment(
            BankAccount from,
            BankAccount to,
            BigDecimal amount,
            String reference) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .debtorAccount(from)
                .creditorAccount(to)
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .endToEndReference(reference)
                .remittanceInformation("Invoice payment")
                .settlementSpeed(SettlementSpeed.STANDARD)
                .build())
            .map(ResponseEntity::getBody)
            .doOnSuccess(response -> 
                log.info("Payment created: {}", response.getPaymentId()))
            .doOnError(error -> 
                log.error("Payment failed", error));
    }
}
```

**Key points**:
- ✅ Single API call
- ✅ No SCA required
- ✅ Suitable for amounts <€30 (SEPA)

---

### Payment with SCA (Two-Phase Commit)

**Use case**: Secure payment requiring Strong Customer Authentication

```java path=null start=null
@Service
public class SecurePaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Phase 1: Authorize payment (may trigger SCA)
     */
    public Mono<AuthorizationResponse> authorizePayment(
            BankAccount from,
            BankAccount to,
            BigDecimal amount,
            String userIp,
            String deviceFingerprint) {
        
        return railAdapter.payments()
            .authorizePayment(AuthorizePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .debtorAccount(from)
                .creditorAccount(to)
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .authenticationContext(new AuthenticationContext(
                    AuthenticationMethod.SMS_OTP,
                    null,
                    null,
                    "https://myapp.com/callback",
                    userIp,
                    deviceFingerprint,
                    Instant.now().plusSeconds(300), // 5 min expiry
                    false,
                    null))
                .build())
            .map(ResponseEntity::getBody)
            .doOnNext(response -> {
                if (response.isScaRequired()) {
                    log.info("SCA required. Auth ID: {}", response.getAuthorizationId());
                    log.info("OTP sent via: {}", response.getScaMethod());
                    // Store authorizationId in session/database
                    // Prompt user for OTP
                } else {
                    log.info("No SCA needed. Auto-confirming...");
                    // Can immediately confirm
                }
            });
    }

    /**
     * Complete SCA authentication with user-provided code
     */
    public Mono<AuthorizationResponse> completeAuthentication(
            String authorizationId,
            String otpCode) {
        
        return railAdapter.payments()
            .completeAuthentication(CompleteAuthenticationRequest.builder()
                .authorizationId(authorizationId)
                .authenticationCode(otpCode)
                .build())
            .map(ResponseEntity::getBody)
            .doOnNext(response -> {
                if (response.isScaCompleted()) {
                    log.info("SCA completed successfully");
                } else {
                    log.warn("SCA failed: {}", response.getErrorMessage());
                }
            });
    }

    /**
     * Phase 2: Confirm and execute the payment
     */
    public Mono<PaymentResponse> confirmPayment(String authorizationId) {
        
        return railAdapter.payments()
            .confirmPayment(authorizationId)
            .map(ResponseEntity::getBody)
            .doOnSuccess(response -> 
                log.info("Payment executed: {}", response.getPaymentId()));
    }

    /**
     * Complete flow: Authorize → Authenticate → Confirm
     */
    public Mono<PaymentResult> executeSecurePayment(
            BankAccount from,
            BankAccount to,
            BigDecimal amount,
            String userIp,
            String deviceFingerprint,
            Supplier<String> otpProvider) {
        
        return authorizePayment(from, to, amount, userIp, deviceFingerprint)
            .flatMap(authResponse -> {
                if (!authResponse.isScaRequired()) {
                    // No SCA needed, confirm immediately
                    return confirmPayment(authResponse.getAuthorizationId())
                        .map(payment -> new PaymentResult(payment, false));
                }
                
                // SCA required, wait for user input
                String authId = authResponse.getAuthorizationId();
                
                // In real app, this would be async (user enters OTP in UI)
                // For demo, using supplier
                String otp = otpProvider.get();
                
                return completeAuthentication(authId, otp)
                    .flatMap(authResult -> {
                        if (authResult.isScaCompleted()) {
                            return confirmPayment(authId)
                                .map(payment -> new PaymentResult(payment, true));
                        } else {
                            return Mono.error(new ScaFailedException(
                                "SCA authentication failed"));
                        }
                    });
            });
    }

    @Data
    @AllArgsConstructor
    public static class PaymentResult {
        private PaymentResponse payment;
        private boolean scaPerformed;
    }
}
```

**Key points**:
- ✅ Three-step process: Authorize → Authenticate → Confirm
- ✅ PSD2/SCA compliant
- ✅ Required for high-value transactions
- ✅ Supports multiple authentication methods (SMS, biometric, hardware token)

---

### SEPA Credit Transfer

**Use case**: Euro payment within SEPA zone

```java path=null start=null
@Service
public class SEPAPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Standard SEPA Credit Transfer (SCT)
     */
    public Mono<PaymentResponse> sendSEPAPayment(
            String debtorIban,
            String debtorBic,
            String creditorIban,
            String creditorBic,
            BigDecimal amount,
            String reference,
            String remittance) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .debtorAccount(BankAccount.fromIban(
                    "John Doe",
                    debtorIban,
                    debtorBic))
                .creditorAccount(BankAccount.fromIban(
                    "Jane Smith",
                    creditorIban,
                    creditorBic))
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .endToEndReference(reference)
                .remittanceInformation(remittance)
                .settlementSpeed(SettlementSpeed.STANDARD)
                .metadata(Map.of("scheme", "SCT"))
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * SEPA Instant Credit Transfer (SCT Inst)
     * Real-time settlement, 10-second confirmation
     */
    public Mono<PaymentResponse> sendInstantSEPAPayment(
            String debtorIban,
            String creditorIban,
            BigDecimal amount) {
        
        // Validate amount (SCT Inst has max €100,000)
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            return Mono.error(new IllegalArgumentException(
                "SCT Inst max amount is €100,000"));
        }
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .debtorAccount(BankAccount.fromIban("Payer", debtorIban, null))
                .creditorAccount(BankAccount.fromIban("Payee", creditorIban, null))
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .settlementSpeed(SettlementSpeed.INSTANT)
                .metadata(Map.of("scheme", "SCT_INST"))
                .build())
            .map(ResponseEntity::getBody)
            .timeout(Duration.ofSeconds(10))
            .onErrorResume(TimeoutException.class, e -> {
                log.warn("Instant payment timeout, falling back to standard");
                return sendSEPAPayment(debtorIban, null, creditorIban, null, 
                    amount, "FALLBACK", "Standard transfer");
            });
    }
}
```

**Key points**:
- ✅ IBAN + BIC validation
- ✅ Standard SCT: D+1 settlement
- ✅ Instant SCT: 10-second settlement, max €100k
- ✅ Automatic fallback for instant failures

---

### SEPA Direct Debit

**Use case**: Recurring payments with mandate

```java path=null start=null
@Service
public class SEPADirectDebitService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Step 1: Create SEPA Direct Debit mandate
     */
    public Mono<MandateResponse> createMandate(
            String debtorIban,
            String debtorName,
            String creditorId,
            BigDecimal maxAmount) {
        
        return railAdapter.mandates()
            .createMandate(CreateMandateRequest.builder()
                .debtorAccount(BankAccount.fromIban(
                    debtorName,
                    debtorIban,
                    null))
                .creditorId(creditorId)
                .mandateType(MandateType.RECURRING)
                .maxAmount(new Money(maxAmount, Currency.EUR))
                .startDate(LocalDate.now())
                .railSpecificData(Map.of(
                    "scheme", "SDD_CORE",
                    "sequenceType", "FRST"
                ))
                .build())
            .map(ResponseEntity::getBody)
            .doOnSuccess(mandate -> 
                log.info("Mandate created: {}", mandate.getMandateId()));
    }

    /**
     * Step 2: Execute direct debit using mandate (First payment)
     */
    public Mono<PaymentResponse> executeFirstDirectDebit(
            String mandateReference,
            BigDecimal amount,
            LocalDate collectionDate) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .transactionType(TransactionType.DIRECT_DEBIT)
                .requestedExecutionDate(collectionDate)
                .remittanceInformation("Subscription payment")
                .metadata(Map.of(
                    "mandateReference", mandateReference,
                    "scheme", "SDD_CORE",
                    "sequenceType", "FRST"  // First
                ))
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Execute recurring direct debit (subsequent payments)
     */
    public Mono<PaymentResponse> executeRecurringDirectDebit(
            String mandateReference,
            BigDecimal amount,
            LocalDate collectionDate) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(new Money(amount, Currency.EUR))
                .transactionType(TransactionType.DIRECT_DEBIT)
                .requestedExecutionDate(collectionDate)
                .remittanceInformation("Recurring subscription payment")
                .metadata(Map.of(
                    "mandateReference", mandateReference,
                    "scheme", "SDD_CORE",
                    "sequenceType", "RCUR"  // Recurring
                ))
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Cancel mandate
     */
    public Mono<Void> cancelMandate(String mandateId) {
        return railAdapter.mandates()
            .cancelMandate(mandateId)
            .then();
    }
}
```

**Key points**:
- ✅ Mandate-based payments
- ✅ Sequence types: FRST, RCUR, OOFF, FNAL
- ✅ SDD Core (B2C) or SDD B2B
- ✅ Pre-notification required (14 days for first, 2 days for recurring)

---

### SWIFT International Transfer

**Use case**: Cross-border wire transfer

```java path=null start=null
@Service
public class SWIFTPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Send international SWIFT payment
     */
    public Mono<PaymentResponse> sendSWIFTPayment(
            BankAccount debtor,
            BankAccount creditor,
            Money amount,
            String chargeBearer) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(amount)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transactionType(TransactionType.WIRE_TRANSFER)
                .settlementSpeed(SettlementSpeed.SAME_DAY)
                .remittanceInformation("International payment")
                .metadata(Map.of(
                    "messageType", "MT103",
                    "chargesInstruction", chargeBearer,  // OUR, SHA, BEN
                    "purposeCode", "SUPP"
                ))
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Send SWIFT payment with intermediary bank
     */
    public Mono<PaymentResponse> sendSWIFTWithIntermediary(
            BankAccount debtor,
            BankAccount creditor,
            BankAccount intermediary,
            Money amount) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(amount)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transactionType(TransactionType.WIRE_TRANSFER)
                .metadata(Map.of(
                    "messageType", "MT103",
                    "intermediaryBank", intermediary.getSwiftCode(),
                    "intermediaryBankName", intermediary.getAccountHolderName(),
                    "chargesInstruction", "SHA"
                ))
                .build())
            .map(ResponseEntity::getBody);
    }
}
```

**Key points**:
- ✅ SWIFT codes required
- ✅ Charge bearer: OUR (sender pays), SHA (shared), BEN (beneficiary pays)
- ✅ Support for intermediary banks
- ✅ Multi-currency support

---

### Instant/Real-Time Payments

**Use case**: Real-time payment rails (RTP, FPS, PIX)

```java path=null start=null
@Service
public class InstantPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Send instant payment (US RTP, UK FPS, Brazil PIX, etc.)
     */
    public Mono<PaymentResponse> sendInstantPayment(
            BankAccount from,
            BankAccount to,
            Money amount,
            String message) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(amount)
                .debtorAccount(from)
                .creditorAccount(to)
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .settlementSpeed(SettlementSpeed.INSTANT)
                .remittanceInformation(message)
                .build())
            .map(ResponseEntity::getBody)
            .timeout(Duration.ofSeconds(5))  // Instant payments must be fast
            .doOnSuccess(response -> {
                if (response.getStatus() == PaymentStatus.COMPLETED) {
                    log.info("Instant payment completed in real-time");
                }
            });
    }

    /**
     * Request for Payment (RfP) - Request money from someone
     */
    public Mono<PaymentResponse> requestPayment(
            BankAccount from,
            BankAccount to,
            Money amount,
            String reason,
            LocalDateTime expiryTime) {
        
        return railAdapter.payments()
            .initiatePayment(InitiatePaymentRequest.builder()
                .amount(amount)
                .debtorAccount(from)
                .creditorAccount(to)
                .transactionType(TransactionType.PAYMENT_REQUEST)
                .remittanceInformation(reason)
                .metadata(Map.of(
                    "requestType", "RfP",
                    "expiryTime", expiryTime.toString()
                ))
                .build())
            .map(ResponseEntity::getBody);
    }
}
```

---

## Bulk Operations

### Bulk Payment Submission

```java path=null start=null
@Service
public class BulkPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Submit bulk payments (e.g., payroll)
     */
    public Mono<BulkPaymentResponse> submitPayroll(
            BankAccount companyAccount,
            List<PayrollEntry> employees) {
        
        List<InitiatePaymentRequest> payments = employees.stream()
            .map(emp -> InitiatePaymentRequest.builder()
                .amount(new Money(emp.getSalary(), Currency.EUR))
                .debtorAccount(companyAccount)
                .creditorAccount(emp.getBankAccount())
                .transactionType(TransactionType.CREDIT_TRANSFER)
                .endToEndReference("PAYROLL-" + emp.getEmployeeId())
                .remittanceInformation("Salary payment")
                .build())
            .toList();

        return railAdapter.bulkPayments()
            .submitBulkPayment(BulkPaymentRequest.builder()
                .payments(payments)
                .requestedExecutionDate(LocalDate.now().plusDays(1))
                .batchBooking(true)
                .build())
            .map(ResponseEntity::getBody)
            .doOnSuccess(response -> 
                log.info("Bulk payment submitted: {} payments", response.getTotalPayments()));
    }

    /**
     * Track bulk payment status
     */
    public Mono<BulkPaymentStatusResponse> trackBulkPayment(String bulkPaymentId) {
        return railAdapter.bulkPayments()
            .getBulkPaymentStatus(bulkPaymentId)
            .map(ResponseEntity::getBody);
    }

    /**
     * Cancel bulk payment before execution
     */
    public Mono<Void> cancelBulkPayment(String bulkPaymentId) {
        return railAdapter.bulkPayments()
            .cancelBulkPayment(bulkPaymentId)
            .then();
    }
}
```

---

## Mandate Management

```java path=null start=null
@Service
public class MandateService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Create recurring payment mandate
     */
    public Mono<MandateResponse> createRecurringMandate(
            BankAccount debtorAccount,
            String creditorId,
            Money maxAmount,
            LocalDate startDate,
            LocalDate endDate) {
        
        return railAdapter.mandates()
            .createMandate(CreateMandateRequest.builder()
                .debtorAccount(debtorAccount)
                .creditorId(creditorId)
                .mandateType(MandateType.RECURRING)
                .maxAmount(maxAmount)
                .startDate(startDate)
                .endDate(endDate)
                .frequency("MONTHLY")
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Get mandate details
     */
    public Mono<MandateResponse> getMandate(String mandateId) {
        return railAdapter.mandates()
            .getMandate(mandateId)
            .map(ResponseEntity::getBody);
    }

    /**
     * Update mandate (e.g., increase max amount)
     */
    public Mono<MandateResponse> updateMandate(
            String mandateId,
            Money newMaxAmount) {
        
        return railAdapter.mandates()
            .updateMandate(UpdateMandateRequest.builder()
                .mandateId(mandateId)
                .maxAmount(newMaxAmount)
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * List all mandates for a debtor
     */
    public Mono<List<MandateResponse>> listMandates(BankAccount debtorAccount) {
        return railAdapter.mandates()
            .listMandates(ListMandatesRequest.builder()
                .debtorAccount(debtorAccount)
                .status(MandateStatus.ACTIVE)
                .build())
            .map(ResponseEntity::getBody);
    }
}
```

---

## Payment Tracking

```java path=null start=null
@Service
public class PaymentTrackingService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Get payment by ID
     */
    public Mono<PaymentResponse> getPayment(String paymentId) {
        return railAdapter.payments()
            .getPayment(paymentId)
            .map(ResponseEntity::getBody);
    }

    /**
     * Get payment status by reference
     */
    public Mono<PaymentStatusResponse> getPaymentStatus(String reference) {
        return railAdapter.payments()
            .getPaymentStatus(reference)
            .map(ResponseEntity::getBody);
    }

    /**
     * List payments with filters
     */
    public Mono<List<PaymentResponse>> listPayments(
            LocalDate fromDate,
            LocalDate toDate,
            PaymentStatus status) {
        
        return railAdapter.payments()
            .listPayments(ListPaymentsRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .status(status)
                .pageSize(100)
                .build())
            .map(ResponseEntity::getBody);
    }

    /**
     * Poll payment status until completed or failed
     */
    public Mono<PaymentResponse> waitForCompletion(
            String paymentId,
            Duration timeout) {
        
        return Mono.defer(() -> railAdapter.payments().getPayment(paymentId))
            .map(ResponseEntity::getBody)
            .repeatWhen(repeat -> repeat
                .delayElements(Duration.ofSeconds(2))
                .takeWhile(response -> 
                    response.getStatus() == PaymentStatus.PENDING ||
                    response.getStatus() == PaymentStatus.PROCESSING))
            .timeout(timeout)
            .last();
    }
}
```

---

## Error Handling

```java path=null start=null
@Service
public class RobustPaymentService {

    @Autowired
    private RailAdapter railAdapter;

    /**
     * Payment with comprehensive error handling
     */
    public Mono<PaymentResponse> sendPaymentWithRetry(
            InitiatePaymentRequest request) {
        
        return railAdapter.payments()
            .initiatePayment(request)
            .map(ResponseEntity::getBody)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof TimeoutException))
            .onErrorResume(RailException.class, ex -> {
                log.error("Rail error: {}", ex.getMessage());
                return handleRailException(ex, request);
            })
            .onErrorResume(ValidationException.class, ex -> {
                log.error("Validation error: {}", ex.getErrors());
                return Mono.error(new BadRequestException(ex.getMessage()));
            })
            .doOnError(error -> 
                log.error("Payment failed after retries", error));
    }

    private Mono<PaymentResponse> handleRailException(
            RailException ex,
            InitiatePaymentRequest originalRequest) {
        
        return switch (ex.getErrorCode()) {
            case "INSUFFICIENT_FUNDS" -> 
                Mono.error(new InsufficientFundsException(ex.getMessage()));
            case "INVALID_ACCOUNT" ->
                Mono.error(new InvalidAccountException(ex.getMessage()));
            case "RATE_LIMIT_EXCEEDED" -> {
                log.warn("Rate limited, waiting before retry");
                yield Mono.delay(Duration.ofSeconds(5))
                    .then(sendPaymentWithRetry(originalRequest));
            }
            default ->
                Mono.error(ex);
        };
    }
}
```

---

## Best Practices

### 1. Use Idempotency Keys

```java path=null start=null
public Mono<PaymentResponse> sendIdempotentPayment(
        InitiatePaymentRequest request,
        String clientReference) {
    
    IdempotencyKey key = IdempotencyKey.of(
        clientReference,
        Instant.now().plus(Duration.ofHours(24))
    );
    
    return railAdapter.payments()
        .initiatePayment(request.toBuilder()
            .idempotencyKey(key)
            .build())
        .map(ResponseEntity::getBody);
}
```

### 2. Validate Before Submission

```java path=null start=null
public Mono<PaymentResponse> sendValidatedPayment(
        InitiatePaymentRequest request) {
    
    // Pre-flight validation
    return railAdapter.payments()
        .validatePayment(ValidatePaymentRequest.builder()
            .amount(request.getAmount())
            .debtorAccount(request.getDebtorAccount())
            .creditorAccount(request.getCreditorAccount())
            .build())
        .flatMap(validationResponse -> {
            if (validationResponse.getBody().isValid()) {
                // Validation passed, submit payment
                return railAdapter.payments()
                    .initiatePayment(request)
                    .map(ResponseEntity::getBody);
            } else {
                // Validation failed
                return Mono.error(new ValidationException(
                    validationResponse.getBody().getErrors()));
            }
        });
}
```

### 3. Simulate for Cost Estimation

```java path=null start=null
public Mono<SimulationResponse> estimatePaymentCost(
        InitiatePaymentRequest request) {
    
    return railAdapter.payments()
        .simulatePayment(SimulatePaymentRequest.builder()
            .amount(request.getAmount())
            .debtorAccount(request.getDebtorAccount())
            .creditorAccount(request.getCreditorAccount())
            .settlementSpeed(request.getSettlementSpeed())
            .build())
        .map(ResponseEntity::getBody)
        .doOnNext(simulation -> {
            log.info("Estimated fees: {}", simulation.getEstimatedFees());
            log.info("Settlement date: {}", simulation.getEstimatedSettlementDate());
        });
}
```

### 4. Handle Webhooks for Status Updates

```java path=null start=null
@RestController
@RequestMapping("/webhooks")
public class PaymentWebhookController {

    @Autowired
    private PaymentEventHandler eventHandler;

    @PostMapping("/payment-status")
    public Mono<Void> handlePaymentStatus(@RequestBody PaymentStatusWebhook webhook) {
        return eventHandler.processPaymentStatusUpdate(webhook)
            .doOnSuccess(v -> log.info("Processed webhook: {}", webhook.getPaymentId()))
            .then();
    }
}
```

### 5. Use Reactive Patterns

```java path=null start=null
public Flux<PaymentResponse> sendMultiplePayments(
        List<InitiatePaymentRequest> requests) {
    
    return Flux.fromIterable(requests)
        .flatMap(request -> railAdapter.payments()
            .initiatePayment(request)
            .map(ResponseEntity::getBody)
            .onErrorResume(error -> {
                log.error("Payment failed: {}", error.getMessage());
                return Mono.empty();  // Continue with other payments
            }))
        .collectList()
        .flatMapMany(Flux::fromIterable);
}
```

---

## Next Steps

- Review [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) to implement new rails
- Check [ARCHITECTURE.md](ARCHITECTURE.md) for design details
- See [TESTING_GUIDE.md](TESTING_GUIDE.md) for testing strategies
- Explore [README.md](../README.md) for quick start

---

**License**: Apache 2.0  
**Copyright**: 2025 Firefly Software Solutions Inc
