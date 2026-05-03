/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter;

import com.firefly.rails.adapter.ports.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for RailAdapter interface abstraction.
 * Validates the core business proposition: rail-independent unified interface.
 */
@DisplayName("RailAdapter Abstraction Tests")
class RailAdapterTest {

    @Mock private PaymentRailPort paymentRailPort;
    @Mock private SettlementPort settlementPort;
    @Mock private StatusPort statusPort;
    @Mock private MandatePort mandatePort;
    @Mock private BulkPaymentPort bulkPaymentPort;
    @Mock private ReconciliationPort reconciliationPort;
    @Mock private RailSpecificPort railSpecificPort;
    @Mock private ScheduledPaymentPort scheduledPaymentPort;
    @Mock private CompliancePort compliancePort;

    private RailAdapter railAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Rail Independence Tests")
    class RailIndependenceTests {

        @Test
        @DisplayName("Should provide same interface for ACH rail")
        void shouldProvideSameInterfaceForACH() {
            // Given: ACH rail adapter
            railAdapter = new TestRailAdapter("ach", paymentRailPort, settlementPort, 
                statusPort, mandatePort, bulkPaymentPort, reconciliationPort, 
                railSpecificPort, scheduledPaymentPort, compliancePort);

            // When & Then: All ports accessible
            assertThat(railAdapter.payments()).isNotNull();
            assertThat(railAdapter.settlement()).isNotNull();
            assertThat(railAdapter.status()).isNotNull();
            assertThat(railAdapter.mandates()).isNotNull();
            assertThat(railAdapter.bulkPayments()).isNotNull();
            assertThat(railAdapter.reconciliation()).isNotNull();
            assertThat(railAdapter.railSpecific()).isNotNull();
            assertThat(railAdapter.scheduledPayments()).isNotNull();
            assertThat(railAdapter.compliance()).isNotNull();
            assertThat(railAdapter.getRailType()).isEqualTo("ach");
        }

        @Test
        @DisplayName("Should provide same interface for SEPA rail")
        void shouldProvideSameInterfaceForSEPA() {
            // Given: SEPA rail adapter
            railAdapter = new TestRailAdapter("sepa", paymentRailPort, settlementPort, 
                statusPort, mandatePort, bulkPaymentPort, reconciliationPort, 
                railSpecificPort, scheduledPaymentPort, compliancePort);

            // When & Then: All ports accessible with same interface
            assertThat(railAdapter.payments()).isNotNull();
            assertThat(railAdapter.settlement()).isNotNull();
            assertThat(railAdapter.getRailType()).isEqualTo("sepa");
        }

        @Test
        @DisplayName("Should provide same interface for SWIFT rail")
        void shouldProvideSameInterfaceForSWIFT() {
            // Given: SWIFT rail adapter
            railAdapter = new TestRailAdapter("swift", paymentRailPort, settlementPort, 
                statusPort, mandatePort, bulkPaymentPort, reconciliationPort, 
                railSpecificPort, scheduledPaymentPort, compliancePort);

            // When & Then: All ports accessible with same interface
            assertThat(railAdapter.payments()).isNotNull();
            assertThat(railAdapter.getRailType()).isEqualTo("swift");
        }

        @Test
        @DisplayName("Should provide same interface for RTP rail")
        void shouldProvideSameInterfaceForRTP() {
            // Given: RTP rail adapter
            railAdapter = new TestRailAdapter("rtp", paymentRailPort, settlementPort, 
                statusPort, mandatePort, bulkPaymentPort, reconciliationPort, 
                railSpecificPort, scheduledPaymentPort, compliancePort);

            // When & Then: All ports accessible with same interface
            assertThat(railAdapter.payments()).isNotNull();
            assertThat(railAdapter.getRailType()).isEqualTo("rtp");
        }
    }

    @Nested
    @DisplayName("Port Access Tests")
    class PortAccessTests {

        @BeforeEach
        void setUp() {
            railAdapter = new TestRailAdapter("test-rail", paymentRailPort, 
                settlementPort, statusPort, mandatePort, bulkPaymentPort, 
                reconciliationPort, railSpecificPort, scheduledPaymentPort, compliancePort);
        }

        @Test
        @DisplayName("Should provide access to PaymentRailPort")
        void shouldProvideAccessToPaymentRailPort() {
            assertThat(railAdapter.payments()).isSameAs(paymentRailPort);
        }

        @Test
        @DisplayName("Should provide access to SettlementPort")
        void shouldProvideAccessToSettlementPort() {
            assertThat(railAdapter.settlement()).isSameAs(settlementPort);
        }

        @Test
        @DisplayName("Should provide access to StatusPort")
        void shouldProvideAccessToStatusPort() {
            assertThat(railAdapter.status()).isSameAs(statusPort);
        }

        @Test
        @DisplayName("Should provide access to MandatePort")
        void shouldProvideAccessToMandatePort() {
            assertThat(railAdapter.mandates()).isSameAs(mandatePort);
        }

        @Test
        @DisplayName("Should provide access to BulkPaymentPort")
        void shouldProvideAccessToBulkPaymentPort() {
            assertThat(railAdapter.bulkPayments()).isSameAs(bulkPaymentPort);
        }

        @Test
        @DisplayName("Should provide access to ReconciliationPort")
        void shouldProvideAccessToReconciliationPort() {
            assertThat(railAdapter.reconciliation()).isSameAs(reconciliationPort);
        }

        @Test
        @DisplayName("Should provide access to RailSpecificPort")
        void shouldProvideAccessToRailSpecificPort() {
            assertThat(railAdapter.railSpecific()).isSameAs(railSpecificPort);
        }

        @Test
        @DisplayName("Should provide access to ScheduledPaymentPort")
        void shouldProvideAccessToScheduledPaymentPort() {
            assertThat(railAdapter.scheduledPayments()).isSameAs(scheduledPaymentPort);
        }

        @Test
        @DisplayName("Should provide access to CompliancePort")
        void shouldProvideAccessToCompliancePort() {
            assertThat(railAdapter.compliance()).isSameAs(compliancePort);
        }

        @Test
        @DisplayName("Should provide all 9 ports")
        void shouldProvideAllNinePorts() {
            // Business value: Complete feature coverage through 9 specialized ports
            assertThat(railAdapter.payments()).isNotNull();
            assertThat(railAdapter.settlement()).isNotNull();
            assertThat(railAdapter.status()).isNotNull();
            assertThat(railAdapter.mandates()).isNotNull();
            assertThat(railAdapter.bulkPayments()).isNotNull();
            assertThat(railAdapter.reconciliation()).isNotNull();
            assertThat(railAdapter.railSpecific()).isNotNull();
            assertThat(railAdapter.scheduledPayments()).isNotNull();
            assertThat(railAdapter.compliance()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should report healthy when rail is connected")
        void shouldReportHealthyWhenRailConnected() {
            // Given: Healthy rail adapter
            railAdapter = new TestRailAdapter("test-rail", true, paymentRailPort, 
                settlementPort, statusPort, mandatePort, bulkPaymentPort, 
                reconciliationPort, railSpecificPort, scheduledPaymentPort, compliancePort);

            // When & Then
            assertThat(railAdapter.isHealthy()).isTrue();
        }

        @Test
        @DisplayName("Should report unhealthy when rail is disconnected")
        void shouldReportUnhealthyWhenRailDisconnected() {
            // Given: Unhealthy rail adapter
            railAdapter = new TestRailAdapter("test-rail", false, paymentRailPort, 
                settlementPort, statusPort, mandatePort, bulkPaymentPort, 
                reconciliationPort, railSpecificPort, scheduledPaymentPort, compliancePort);

            // When & Then
            assertThat(railAdapter.isHealthy()).isFalse();
        }
    }

    @Nested
    @DisplayName("Hexagonal Architecture Tests")
    class HexagonalArchitectureTests {

        @Test
        @DisplayName("Should follow ports and adapters pattern")
        void shouldFollowPortsAndAdaptersPattern() {
            // Given: Rail adapter with all ports
            railAdapter = new TestRailAdapter("test-rail", paymentRailPort, 
                settlementPort, statusPort, mandatePort, bulkPaymentPort, 
                reconciliationPort, railSpecificPort, scheduledPaymentPort, compliancePort);

            // When & Then: Adapter exposes ports, not implementation details
            assertThat(railAdapter.payments()).isInstanceOf(PaymentRailPort.class);
            assertThat(railAdapter.settlement()).isInstanceOf(SettlementPort.class);
            assertThat(railAdapter.status()).isInstanceOf(StatusPort.class);
            assertThat(railAdapter.mandates()).isInstanceOf(MandatePort.class);
            assertThat(railAdapter.bulkPayments()).isInstanceOf(BulkPaymentPort.class);
            assertThat(railAdapter.reconciliation()).isInstanceOf(ReconciliationPort.class);
            assertThat(railAdapter.railSpecific()).isInstanceOf(RailSpecificPort.class);
            assertThat(railAdapter.scheduledPayments()).isInstanceOf(ScheduledPaymentPort.class);
            assertThat(railAdapter.compliance()).isInstanceOf(CompliancePort.class);
        }

        @Test
        @DisplayName("Should allow rail switching via configuration")
        void shouldAllowRailSwitchingViaConfiguration() {
            // Business value: Switch rails without code changes
            
            // Switch to ACH
            RailAdapter achAdapter = new TestRailAdapter("ach", paymentRailPort, 
                settlementPort, statusPort, mandatePort, bulkPaymentPort, 
                reconciliationPort, railSpecificPort, scheduledPaymentPort, compliancePort);
            assertThat(achAdapter.getRailType()).isEqualTo("ach");

            // Switch to SEPA
            RailAdapter sepaAdapter = new TestRailAdapter("sepa", paymentRailPort, 
                settlementPort, statusPort, mandatePort, bulkPaymentPort, 
                reconciliationPort, railSpecificPort, scheduledPaymentPort, compliancePort);
            assertThat(sepaAdapter.getRailType()).isEqualTo("sepa");

            // Same interface, different rail
            assertThat(achAdapter.payments()).isNotNull();
            assertThat(sepaAdapter.payments()).isNotNull();
        }
    }

    /**
     * Test implementation of RailAdapter for testing purposes.
     */
    private static class TestRailAdapter implements RailAdapter {
        private final String railType;
        private final boolean healthy;
        private final PaymentRailPort paymentRailPort;
        private final SettlementPort settlementPort;
        private final StatusPort statusPort;
        private final MandatePort mandatePort;
        private final BulkPaymentPort bulkPaymentPort;
        private final ReconciliationPort reconciliationPort;
        private final RailSpecificPort railSpecificPort;
        private final ScheduledPaymentPort scheduledPaymentPort;
        private final CompliancePort compliancePort;

        TestRailAdapter(String railType, PaymentRailPort paymentRailPort, 
                       SettlementPort settlementPort, StatusPort statusPort, 
                       MandatePort mandatePort, BulkPaymentPort bulkPaymentPort, 
                       ReconciliationPort reconciliationPort, 
                       RailSpecificPort railSpecificPort, 
                       ScheduledPaymentPort scheduledPaymentPort, 
                       CompliancePort compliancePort) {
            this(railType, true, paymentRailPort, settlementPort, statusPort, 
                 mandatePort, bulkPaymentPort, reconciliationPort, 
                 railSpecificPort, scheduledPaymentPort, compliancePort);
        }

        TestRailAdapter(String railType, boolean healthy, PaymentRailPort paymentRailPort, 
                       SettlementPort settlementPort, StatusPort statusPort, 
                       MandatePort mandatePort, BulkPaymentPort bulkPaymentPort, 
                       ReconciliationPort reconciliationPort, 
                       RailSpecificPort railSpecificPort, 
                       ScheduledPaymentPort scheduledPaymentPort, 
                       CompliancePort compliancePort) {
            this.railType = railType;
            this.healthy = healthy;
            this.paymentRailPort = paymentRailPort;
            this.settlementPort = settlementPort;
            this.statusPort = statusPort;
            this.mandatePort = mandatePort;
            this.bulkPaymentPort = bulkPaymentPort;
            this.reconciliationPort = reconciliationPort;
            this.railSpecificPort = railSpecificPort;
            this.scheduledPaymentPort = scheduledPaymentPort;
            this.compliancePort = compliancePort;
        }

        @Override public PaymentRailPort payments() { return paymentRailPort; }
        @Override public SettlementPort settlement() { return settlementPort; }
        @Override public StatusPort status() { return statusPort; }
        @Override public MandatePort mandates() { return mandatePort; }
        @Override public BulkPaymentPort bulkPayments() { return bulkPaymentPort; }
        @Override public ReconciliationPort reconciliation() { return reconciliationPort; }
        @Override public RailSpecificPort railSpecific() { return railSpecificPort; }
        @Override public ScheduledPaymentPort scheduledPayments() { return scheduledPaymentPort; }
        @Override public CompliancePort compliance() { return compliancePort; }
        @Override public String getRailType() { return railType; }
        @Override public boolean isHealthy() { return healthy; }
    }
}
