package com.mavi.kommo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mavi.kommo.dto.FormPayload;
import com.mavi.kommo.service.LeadCreationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

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
    public ResponseEntity<Map<String, Object>> receiveForm(@RequestBody FormPayload payload) {
        JsonNode result = leadCreationService.createLead(payload);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lead created successfully",
                "data", result != null ? result : Map.of()
        ));
    }
}
