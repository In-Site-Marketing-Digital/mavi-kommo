package com.mavi.kommo.controller;

import com.mavi.kommo.domain.IntegrationSettings;
import com.mavi.kommo.dto.Pipeline;
import com.mavi.kommo.repository.SettingsRepository;
import com.mavi.kommo.service.KommoApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    private final SettingsRepository settingsRepository;
    private final KommoApiService kommoApiService;

    public SettingsController(SettingsRepository settingsRepository, KommoApiService kommoApiService) {
        this.settingsRepository = settingsRepository;
        this.kommoApiService = kommoApiService;
    }

    @GetMapping("/settings")
    public ResponseEntity<IntegrationSettings> getSettings() {
        IntegrationSettings settings = settingsRepository.getSettings();
        log.info("Returning integration settings: hasDirections={}", settings != null && settings.getDirections() != null);
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/settings")
    public ResponseEntity<IntegrationSettings> saveSettings(@RequestBody IntegrationSettings newSettings) {
        log.info(
                "Saving integration settings: legacyPipelineId={}, legacyStatusId={}, directionKeys={}",
                newSettings != null ? newSettings.getPipelineId() : null,
                newSettings != null ? newSettings.getStatusId() : null,
                newSettings != null && newSettings.getDirections() != null ? newSettings.getDirections().keySet() : List.of()
        );
        settingsRepository.saveSettings(newSettings);
        return ResponseEntity.ok(settingsRepository.getSettings());
    }

    @GetMapping("/kommo/pipelines")
    public ResponseEntity<List<Pipeline>> getPipelines() {
        List<Pipeline> pipelines = kommoApiService.getPipelines();
        log.info("Returning Kommo pipelines: count={}", pipelines.size());
        return ResponseEntity.ok(pipelines);
    }
}
