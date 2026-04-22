package com.mavi.kommo.controller;

import com.mavi.kommo.domain.FieldMapping;
import com.mavi.kommo.dto.SaveMappingsRequest;
import com.mavi.kommo.service.MappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mappings")
public class MappingController {

    private final MappingService mappingService;

    public MappingController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @GetMapping
    public ResponseEntity<List<FieldMapping>> getMappings() {
        return ResponseEntity.ok(mappingService.getAllMappings());
    }

    @PostMapping
    public ResponseEntity<List<FieldMapping>> saveMappings(@RequestBody SaveMappingsRequest request) {
        List<FieldMapping> saved = mappingService.saveMappings(request.getMappings());
        return ResponseEntity.ok(saved);
    }
}
