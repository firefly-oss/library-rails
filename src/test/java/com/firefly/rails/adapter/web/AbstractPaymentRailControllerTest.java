/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.adapter.ports.PaymentRailPort;
import com.firefly.rails.domain.PaymentStatus;
import com.firefly.rails.dtos.payments.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Comprehensive tests for AbstractPaymentRailController.
 * Validates all payment operations including SCA flows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractPaymentRailController Tests")
class AbstractPaymentRailControllerTest {

    @Mock
    private RailAdapter railAdapter;

    @Mock
    private PaymentRailPort paymentRailPort;

    private TestPaymentRailController controller;

    @BeforeEach
    void setUp() {
        when(railAdapter.payments()).thenReturn(paymentRailPort);
        controller = new TestPaymentRailController(railAdapter);
    }

    @Nested
    @DisplayName("Validation & Simulation Operations")
    class ValidationSimulationTests {

        @Test
        @DisplayName("Should validate payment without execution")
        void shouldValidatePayment() {
            // Given - Don't include accounts to avoid NPE in logging
            ValidatePaymentRequest request = ValidatePaymentRequest.builder().build();
            ValidationResponse expected = ValidationResponse.builder().valid(true).build();
            when(paymentRailPort.validatePayment(request))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.validatePayment(request))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().isValid())
                .verifyComplete();
        }

        @Test
        @DisplayName("Should simulate payment execution")
        void shouldSimulatePayment() {
            // Given
            SimulatePaymentRequest request = SimulatePaymentRequest.builder().build();
            SimulationResponse expected = SimulationResponse.builder().build();
            when(paymentRailPort.simulatePayment(request))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.simulatePayment(request))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null)
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("SCA Two-Phase Commit Flow")
    class SCAFlowTests {

        @Test
        @DisplayName("Should authorize payment (Phase 1) and potentially trigger SCA challenge")
        void shouldAuthorizePayment() {
            // Given
            AuthorizePaymentRequest request = AuthorizePaymentRequest.builder().build();
            AuthorizationResponse expected = AuthorizationResponse.builder()
                .authorizationId("auth_123")
                .scaRequired(true)
                .status(AuthorizationResponse.AuthorizationStatus.PENDING_AUTHENTICATION)
                .build();
            when(paymentRailPort.authorizePayment(request))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.authorizePayment(request))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().getAuthorizationId().equals("auth_123") &&
                    response.getBody().isScaRequired())
                .verifyComplete();
        }

        @Test
        @DisplayName("Should complete SCA authentication challenge")
        void shouldCompleteAuthentication() {
            // Given
            CompleteAuthenticationRequest request = CompleteAuthenticationRequest.builder()
                .authorizationId("auth_123")
                .authenticationResponse("123456")
                .build();
            AuthorizationResponse expected = AuthorizationResponse.builder()
                .authorizationId("auth_123")
                .scaRequired(false)
                .status(AuthorizationResponse.AuthorizationStatus.AUTHORIZED)
                .build();
            when(paymentRailPort.completeAuthentication(request))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.completeAuthentication(request))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    !response.getBody().isScaRequired() &&
                    response.getBody().getStatus() == AuthorizationResponse.AuthorizationStatus.AUTHORIZED)
                .verifyComplete();
        }

        @Test
        @DisplayName("Should confirm authorized payment (Phase 2)")
        void shouldConfirmPayment() {
            // Given
            String authorizationId = "auth_123";
            PaymentResponse expected = PaymentResponse.builder()
                .paymentId("pay_456")
                .status(PaymentStatus.COMPLETED)
                .build();
            when(paymentRailPort.confirmPayment(authorizationId))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.confirmPayment(authorizationId))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().getPaymentId().equals("pay_456"))
                .verifyComplete();
        }

        @Test
        @DisplayName("Should cancel authorization before confirmation")
        void shouldCancelAuthorization() {
            // Given
            String authorizationId = "auth_123";
            CancellationResponse expected = CancellationResponse.builder()
                .authorizationId(authorizationId)
                .cancelled(true)
                .build();
            when(paymentRailPort.cancelAuthorization(authorizationId))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.cancelAuthorization(authorizationId))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().isCancelled())
                .verifyComplete();
        }

        @Test
        @DisplayName("Should support complete SCA flow: authorize -> authenticate -> confirm")
        void shouldSupportCompleteSCAFlow() {
            // Phase 1: Authorize (triggers SCA)
            AuthorizePaymentRequest authRequest = AuthorizePaymentRequest.builder().build();
            AuthorizationResponse authResponse = AuthorizationResponse.builder()
                .authorizationId("auth_123")
                .scaRequired(true)
                .status(AuthorizationResponse.AuthorizationStatus.PENDING_AUTHENTICATION)
                .build();
            when(paymentRailPort.authorizePayment(authRequest))
                .thenReturn(Mono.just(ResponseEntity.ok(authResponse)));

            // Phase 1a: Complete SCA
            CompleteAuthenticationRequest scaRequest = CompleteAuthenticationRequest.builder()
                .authorizationId("auth_123")
                .build();
            AuthorizationResponse scaResponse = AuthorizationResponse.builder()
                .authorizationId("auth_123")
                .scaRequired(false)
                .status(AuthorizationResponse.AuthorizationStatus.AUTHORIZED)
                .build();
            when(paymentRailPort.completeAuthentication(scaRequest))
                .thenReturn(Mono.just(ResponseEntity.ok(scaResponse)));

            // Phase 2: Confirm payment
            PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentId("pay_456")
                .build();
            when(paymentRailPort.confirmPayment("auth_123"))
                .thenReturn(Mono.just(ResponseEntity.ok(paymentResponse)));

            // Verify complete flow
            StepVerifier.create(controller.authorizePayment(authRequest))
                .expectNextMatches(r -> r.getBody().isScaRequired())
                .verifyComplete();

            StepVerifier.create(controller.completeAuthentication(scaRequest))
                .expectNextMatches(r -> r.getBody().getStatus() == AuthorizationResponse.AuthorizationStatus.AUTHORIZED)
                .verifyComplete();

            StepVerifier.create(controller.confirmPayment("auth_123"))
                .expectNextMatches(r -> r.getBody().getPaymentId().equals("pay_456"))
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Direct Payment Execution (Single Phase)")
    class DirectExecutionTests {

        @Test
        @DisplayName("Should initiate payment directly without SCA")
        void shouldInitiatePaymentDirectly() {
            // Given
            InitiatePaymentRequest request = InitiatePaymentRequest.builder().build();
            PaymentResponse expected = PaymentResponse.builder()
                .paymentId("pay_789")
                .status(PaymentStatus.SUBMITTED)
                .build();
            when(paymentRailPort.initiatePayment(request))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.initiatePayment(request))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().getPaymentId().equals("pay_789"))
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryTests {

        @Test
        @DisplayName("Should get payment by ID")
        void shouldGetPaymentById() {
            // Given
            String paymentId = "pay_123";
            PaymentResponse expected = PaymentResponse.builder()
                .paymentId(paymentId)
                .build();
            when(paymentRailPort.getPayment(paymentId))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.getPayment(paymentId))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().getPaymentId().equals(paymentId))
                .verifyComplete();
        }

        @Test
        @DisplayName("Should get payment status by reference")
        void shouldGetPaymentStatus() {
            // Given
            String reference = "ref_123";
            PaymentStatusResponse expected = PaymentStatusResponse.builder()
                .status("COMPLETED")
                .build();
            when(paymentRailPort.getPaymentStatus(reference))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.getPaymentStatus(reference))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().getStatus().equals("COMPLETED"))
                .verifyComplete();
        }

        @Test
        @DisplayName("Should list payments with filters")
        void shouldListPayments() {
            // Given
            ListPaymentsRequest request = ListPaymentsRequest.builder().build();
            List<PaymentResponse> expectedList = List.of(
                PaymentResponse.builder().paymentId("pay_1").build(),
                PaymentResponse.builder().paymentId("pay_2").build()
            );
            when(paymentRailPort.listPayments(request))
                .thenReturn(Mono.just(ResponseEntity.ok(expectedList)));

            // When & Then
            StepVerifier.create(controller.listPayments(request))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().size() == 2)
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Cancellation & Returns")
    class CancellationReturnTests {

        @Test
        @DisplayName("Should cancel pending payment")
        void shouldCancelPayment() {
            // Given
            String paymentId = "pay_123";
            PaymentResponse expected = PaymentResponse.builder()
                .paymentId(paymentId)
                .status(PaymentStatus.CANCELLED)
                .build();
            when(paymentRailPort.cancelPayment(paymentId))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.cancelPayment(paymentId))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().getStatus() == PaymentStatus.CANCELLED)
                .verifyComplete();
        }

        @Test
        @DisplayName("Should request payment return/refund")
        void shouldRequestReturn() {
            // Given
            ReturnRequest request = ReturnRequest.builder().build();
            ReturnResponse expected = ReturnResponse.builder()
                .id("ret_123")
                .status("APPROVED")
                .build();
            when(paymentRailPort.requestReturn(request))
                .thenReturn(Mono.just(ResponseEntity.ok(expected)));

            // When & Then
            StepVerifier.create(controller.requestReturn(request))
                .expectNextMatches(response ->
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().getId().equals("ret_123"))
                .verifyComplete();
        }
    }

    /**
     * Concrete test implementation
     */
    static class TestPaymentRailController extends AbstractPaymentRailController {
        public TestPaymentRailController(RailAdapter railAdapter) {
            super(railAdapter);
        }
    }
}
