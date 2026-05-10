package com.mavi.kommo.controller;

import com.mavi.kommo.domain.FieldMapping;
import com.mavi.kommo.dto.SaveMappingsRequest;
import com.mavi.kommo.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mappings")
public class MappingController {

    private static final Logger log = LoggerFactory.getLogger(MappingController.class);

    private final MappingService mappingService;

    public MappingController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @GetMapping
    public ResponseEntity<List<FieldMapping>> getMappings() {
        List<FieldMapping> mappings = mappingService.getAllMappings();
        log.info("Returning field mappings: count={}", mappings.size());
        return ResponseEntity.ok(mappings);
    }

    @PostMapping
    public ResponseEntity<List<FieldMapping>> saveMappings(@RequestBody SaveMappingsRequest request) {
        List<FieldMapping> requestedMappings =
                request != null && request.getMappings() != null ? request.getMappings() : List.of();

        log.info("Saving field mappings: requestedCount={}", requestedMappings.size());
        List<FieldMapping> saved = mappingService.saveMappings(requestedMappings);
        log.info("Saved field mappings: count={}", saved.size());
        return ResponseEntity.ok(saved);
    }
}
