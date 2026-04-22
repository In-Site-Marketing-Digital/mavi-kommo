package com.mavi.kommo.controller;

import com.mavi.kommo.dto.KommoFieldsResponse;
import com.mavi.kommo.service.KommoApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kommo")
public class FieldsController {

    private final KommoApiService kommoApiService;

    public FieldsController(KommoApiService kommoApiService) {
        this.kommoApiService = kommoApiService;
    }

    /**
     * Returns all available Kommo fields grouped by entity type
     * (lead, contact, company) including both standard and custom fields.
     */
    @GetMapping("/fields")
    public ResponseEntity<KommoFieldsResponse> getFields() {
        return ResponseEntity.ok(KommoFieldsResponse.builder()
                .lead(kommoApiService.getLeadCustomFields())
                .contact(kommoApiService.getContactCustomFields())
                .company(kommoApiService.getCompanyCustomFields())
                .build());
    }
}
