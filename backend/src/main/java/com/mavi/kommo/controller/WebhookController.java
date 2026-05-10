package com.mavi.kommo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mavi.kommo.dto.FormPayload;
import com.mavi.kommo.service.LeadCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final LeadCreationService leadCreationService;

    public WebhookController(LeadCreationService leadCreationService) {
        this.leadCreationService = leadCreationService;
    }

    /**
     * Entry point for the external form.
     * Receives the fixed payload, maps fields according to user configuration,
     * and creates a lead (with contact) in Kommo.
     */
    @PostMapping("/form")
    public ResponseEntity<Map<String, Object>> receiveForm(
            @RequestParam(required = false) String direction,
            @RequestBody FormPayload payload) {
        String resolvedDirection = resolveDirection(direction, payload);

        log.info(
                "Webhook form received: queryDirection={}, payloadDirection={}, resolvedDirection={}, fieldKeys={}, utmKeys={}",
                blankToNull(direction),
                payload != null ? blankToNull(payload.getDirection()) : null,
                blankToNull(resolvedDirection),
                keys(payload != null ? payload.getFields() : null),
                keys(payload != null ? payload.getUtms() : null)
        );

        JsonNode result = leadCreationService.createLead(payload, resolvedDirection);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lead created successfully",
                "direction", resolvedDirection != null ? resolvedDirection : "",
                "data", result != null ? result : Map.of()
        ));
    }

    private String resolveDirection(String queryDirection, FormPayload payload) {
        if (queryDirection != null && !queryDirection.isBlank()) {
            return queryDirection;
        }

        return payload != null ? payload.getDirection() : null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Set<String> keys(Map<String, ?> values) {
        return values != null ? values.keySet() : Set.of();
    }
}
