/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.adapter.ports.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Basic instantiation tests for all abstract controllers.
 * Verifies that all controllers can be properly constructed.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("All Abstract Controllers Tests")
class AllControllersTest {

    @Mock private RailAdapter railAdapter;
    @Mock private PaymentRailPort paymentRailPort;
    @Mock private StatusPort statusPort;
    @Mock private SettlementPort settlementPort;
    @Mock private MandatePort mandatePort;
    @Mock private BulkPaymentPort bulkPaymentPort;
    @Mock private ReconciliationPort reconciliationPort;
    @Mock private ScheduledPaymentPort scheduledPaymentPort;
    @Mock private CompliancePort compliancePort;
    @Mock private RailSpecificPort railSpecificPort;

    @Test
    @DisplayName("Should instantiate AbstractPaymentRailController")
    void shouldInstantiatePaymentController() {
        when(railAdapter.payments()).thenReturn(paymentRailPort);
        AbstractPaymentRailController controller = new TestPaymentRailController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractStatusController")
    void shouldInstantiateStatusController() {
        when(railAdapter.status()).thenReturn(statusPort);
        AbstractStatusController controller = new TestStatusController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractSettlementController")
    void shouldInstantiateSettlementController() {
        when(railAdapter.settlement()).thenReturn(settlementPort);
        AbstractSettlementController controller = new TestSettlementController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractMandateController")
    void shouldInstantiateMandateController() {
        when(railAdapter.mandates()).thenReturn(mandatePort);
        AbstractMandateController controller = new TestMandateController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractBulkPaymentController")
    void shouldInstantiateBulkPaymentController() {
        when(railAdapter.bulkPayments()).thenReturn(bulkPaymentPort);
        AbstractBulkPaymentController controller = new TestBulkPaymentController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractReconciliationController")
    void shouldInstantiateReconciliationController() {
        when(railAdapter.reconciliation()).thenReturn(reconciliationPort);
        AbstractReconciliationController controller = new TestReconciliationController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractScheduledPaymentController")
    void shouldInstantiateScheduledPaymentController() {
        when(railAdapter.scheduledPayments()).thenReturn(scheduledPaymentPort);
        AbstractScheduledPaymentController controller = new TestScheduledPaymentController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractComplianceController")
    void shouldInstantiateComplianceController() {
        when(railAdapter.compliance()).thenReturn(compliancePort);
        AbstractComplianceController controller = new TestComplianceController(railAdapter);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should instantiate AbstractRailSpecificController")
    void shouldInstantiateRailSpecificController() {
        when(railAdapter.railSpecific()).thenReturn(railSpecificPort);
        AbstractRailSpecificController controller = new TestRailSpecificController(railAdapter);
        assertThat(controller).isNotNull();
    }

    // Test implementations
    static class TestPaymentRailController extends AbstractPaymentRailController {
        TestPaymentRailController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestStatusController extends AbstractStatusController {
        TestStatusController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestSettlementController extends AbstractSettlementController {
        TestSettlementController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestMandateController extends AbstractMandateController {
        TestMandateController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestBulkPaymentController extends AbstractBulkPaymentController {
        TestBulkPaymentController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestReconciliationController extends AbstractReconciliationController {
        TestReconciliationController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestScheduledPaymentController extends AbstractScheduledPaymentController {
        TestScheduledPaymentController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestComplianceController extends AbstractComplianceController {
        TestComplianceController(RailAdapter railAdapter) { super(railAdapter); }
    }

    static class TestRailSpecificController extends AbstractRailSpecificController {
        TestRailSpecificController(RailAdapter railAdapter) { super(railAdapter); }
    }
}
