package com.mavi.kommo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavi.kommo.config.StorageProperties;
import com.mavi.kommo.domain.IntegrationSettings;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;

@Repository
public class JsonSettingsRepository implements SettingsRepository {

    private static final Logger log = LoggerFactory.getLogger(JsonSettingsRepository.class);
    private final File settingsFile;
    private final ObjectMapper objectMapper;
    private IntegrationSettings cachedSettings;

    public JsonSettingsRepository(StorageProperties storageProperties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        File dir = new File(storageProperties.getDataDir());
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Could not create data directory: {}", storageProperties.getDataDir());
        }
        this.settingsFile = new File(dir, "settings.json");
    }

    @PostConstruct
    public void init() {
        if (settingsFile.exists()) {
            try {
                this.cachedSettings = objectMapper.readValue(settingsFile, IntegrationSettings.class);
            } catch (IOException e) {
                log.error("Failed to load settings.json", e);
                this.cachedSettings = new IntegrationSettings();
            }
        } else {
            this.cachedSettings = new IntegrationSettings();
        }
    }

    @Override
    public IntegrationSettings getSettings() {
        return cachedSettings;
    }

    @Override
    public void saveSettings(IntegrationSettings settings) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, settings);
            this.cachedSettings = settings;
            log.info("Saved settings to {}", settingsFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save settings.json", e);
            throw new RuntimeException("Could not save settings", e);
        }
    }
}
