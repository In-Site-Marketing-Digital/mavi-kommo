package com.mavi.kommo.service;

import com.fasterxml.jackson.databind.node.NullNode;
import com.mavi.kommo.domain.FunnelDestination;
import com.mavi.kommo.domain.IntegrationSettings;
import com.mavi.kommo.domain.KommoToken;
import com.mavi.kommo.dto.FormPayload;
import com.mavi.kommo.repository.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadCreationServiceTest {

    private MappingService mappingService;
    private KommoApiService kommoApiService;
    private OAuthService oAuthService;
    private SettingsRepository settingsRepository;
    private LeadCreationService service;

    @BeforeEach
    void setUp() {
        mappingService = mock(MappingService.class);
        kommoApiService = mock(KommoApiService.class);
        oAuthService = mock(OAuthService.class);
        settingsRepository = mock(SettingsRepository.class);

        service = new LeadCreationService(
                mappingService,
                kommoApiService,
                oAuthService,
                settingsRepository
        );

        when(mappingService.getAllMappings()).thenReturn(List.of());
        when(oAuthService.getValidToken()).thenReturn(KommoToken.builder()
                .accessToken("access-token")
                .accountDomain("example.kommo.com")
                .build());
        when(kommoApiService.post(
                eq("/api/v4/leads/complex"),
                any(),
                eq("access-token"),
                eq("example.kommo.com")
        )).thenReturn(ResponseEntity.ok(NullNode.getInstance()));
    }

    @Test
    void directionDestinationIsUsedWhenConfigured() {
        when(settingsRepository.getSettings()).thenReturn(IntegrationSettings.builder()
                .pipelineId(111)
                .statusId(222)
                .directions(Map.of(
                        "mavi",
                        FunnelDestination.builder()
                                .pipelineId(333)
                                .statusId(444)
                                .build(),
                        "insite",
                        FunnelDestination.builder()
                                .pipelineId(555)
                                .statusId(666)
                                .build()
                ))
                .build());

        service.createLead(new FormPayload(), "mavi");

        Map<String, Object> lead = captureLeadPayload();
        assertThat(lead).containsEntry("pipeline_id", 333);
        assertThat(lead).containsEntry("status_id", 444);
    }

    @Test
    void explicitDirectionWithoutConfiguredDestinationDoesNotUseAnotherDirectionFallback() {
        when(settingsRepository.getSettings()).thenReturn(IntegrationSettings.builder()
                .pipelineId(111)
                .statusId(222)
                .directions(Map.of(
                        "mavi",
                        FunnelDestination.builder()
                                .pipelineId(333)
                                .statusId(444)
                                .build()
                ))
                .build());

        service.createLead(new FormPayload(), "insite");

        Map<String, Object> lead = captureLeadPayload();
        assertThat(lead).doesNotContainKeys("pipeline_id", "status_id");
    }

    @Test
    void maviDirectionCanUseLegacyDestinationForExistingSettings() {
        when(settingsRepository.getSettings()).thenReturn(IntegrationSettings.builder()
                .pipelineId(111)
                .statusId(222)
                .build());

        service.createLead(new FormPayload(), "mavi");

        Map<String, Object> lead = captureLeadPayload();
        assertThat(lead).containsEntry("pipeline_id", 111);
        assertThat(lead).containsEntry("status_id", 222);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> captureLeadPayload() {
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kommoApiService).post(
                eq("/api/v4/leads/complex"),
                bodyCaptor.capture(),
                eq("access-token"),
                eq("example.kommo.com")
        );

        List<Map<String, Object>> leads = (List<Map<String, Object>>) bodyCaptor.getValue();
        return leads.get(0);
    }
}
