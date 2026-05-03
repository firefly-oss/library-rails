/*
 * Copyright 2025 Firefly Software Foundation
 * Licensed under the Apache License, Version 2.0
 */
package com.firefly.rails.adapter.web;

import com.firefly.rails.adapter.RailAdapter;
import com.firefly.rails.dtos.specific.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRailSpecificController {

    protected final RailAdapter railAdapter;

    @PostMapping("/specific/{operationName}")
    public Mono<ResponseEntity<RailOperationResponse>> executeOperation(
            @PathVariable String operationName,
            @RequestBody RailOperationRequest request) {
        log.info("Executing rail-specific operation: {}", operationName);
        return railAdapter.railSpecific().executeOperation(operationName, request);
    }

    @GetMapping("/specific/supports/{operationName}")
    public ResponseEntity<Boolean> supportsOperation(@PathVariable String operationName) {
        log.debug("Checking if operation is supported: {}", operationName);
        boolean supported = railAdapter.railSpecific().supportsOperation(operationName);
        return ResponseEntity.ok(supported);
    }
}
