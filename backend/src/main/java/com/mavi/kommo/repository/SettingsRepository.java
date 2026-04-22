package com.mavi.kommo.repository;

import com.mavi.kommo.domain.IntegrationSettings;

public interface SettingsRepository {
    IntegrationSettings getSettings();
    void saveSettings(IntegrationSettings settings);
}
