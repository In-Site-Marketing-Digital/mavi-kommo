package com.mavi.kommo.controller;

import com.mavi.kommo.domain.IntegrationSettings;
import com.mavi.kommo.dto.Pipeline;
import com.mavi.kommo.repository.SettingsRepository;
import com.mavi.kommo.service.KommoApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SettingsController {

    private final SettingsRepository settingsRepository;
    private final KommoApiService kommoApiService;

    public SettingsController(SettingsRepository settingsRepository, KommoApiService kommoApiService) {
        this.settingsRepository = settingsRepository;
        this.kommoApiService = kommoApiService;
    }

    @GetMapping("/settings")
    public ResponseEntity<IntegrationSettings> getSettings() {
        return ResponseEntity.ok(settingsRepository.getSettings());
    }

    @PostMapping("/settings")
    public ResponseEntity<IntegrationSettings> saveSettings(@RequestBody IntegrationSettings newSettings) {
        settingsRepository.saveSettings(newSettings);
        return ResponseEntity.ok(settingsRepository.getSettings());
    }

    @GetMapping("/kommo/pipelines")
    public ResponseEntity<List<Pipeline>> getPipelines() {
        return ResponseEntity.ok(kommoApiService.getPipelines());
    }
}
