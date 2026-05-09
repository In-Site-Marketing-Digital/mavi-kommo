package com.mavi.kommo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mavi.kommo.domain.FieldMapping;
import com.mavi.kommo.domain.FunnelDestination;
import com.mavi.kommo.domain.IntegrationSettings;
import com.mavi.kommo.domain.KommoToken;
import com.mavi.kommo.domain.KommoField;
import com.mavi.kommo.dto.FormPayload;
import com.mavi.kommo.repository.SettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Orchestrates lead creation in Kommo based on the incoming form payload and
 * the field mappings configured by the user.
 *
 * <p>Uses {@code /api/v4/leads/complex} which creates a lead + contact atomically.
 */
@Service
public class LeadCreationService {

    private final MappingService mappingService;
    private final KommoApiService kommoApiService;
    private final OAuthService oAuthService;
    private final SettingsRepository settingsRepository;

    public LeadCreationService(
            MappingService mappingService,
            KommoApiService kommoApiService,
            OAuthService oAuthService,
            SettingsRepository settingsRepository) {
        this.mappingService = mappingService;
        this.kommoApiService = kommoApiService;
        this.oAuthService = oAuthService;
        this.settingsRepository = settingsRepository;
    }

    public JsonNode createLead(FormPayload payload) {
        return createLead(payload, null);
    }

    public JsonNode createLead(FormPayload payload, String direction) {
        KommoToken token = oAuthService.getValidToken();
        List<FieldMapping> mappings = mappingService.getAllMappings();
        IntegrationSettings settings = settingsRepository.getSettings();
        FunnelDestination destination = resolveFunnelDestination(settings, direction);

        Map<String, Object> lead = buildLeadPayload(payload, mappings, destination);
        Map<String, Object> contact = buildContactPayload(payload, mappings);

        // Embed contact inside the lead for the complex endpoint
        lead.put("_embedded", Map.of("contacts", List.of(contact)));

        ResponseEntity<JsonNode> response = kommoApiService.post(
                "/api/v4/leads/complex",
                List.of(lead),
                token.getAccessToken(),
                token.getAccountDomain()
        );

        return response.getBody();
    }

    // ── Private builders ───────────────────────────────────────────────────────

    private Map<String, Object> buildLeadPayload(FormPayload payload, List<FieldMapping> mappings, FunnelDestination destination) {
        Map<String, Object> lead = new HashMap<>();
        List<Map<String, Object>> customFields = new ArrayList<>();
        String leadName = "Lead from Form";

        for (FieldMapping mapping : mappings) {
            if (!"lead".equals(mapping.getKommoEntityType())) continue;

            String value = resolveStringValue(payload, mapping.getPayloadField());
            if (value == null) continue;

            if (mapping.isStandard() && "name".equals(mapping.getFieldCode())) {
                leadName = value;
            } else if (!mapping.isStandard()) {
                customFields.add(buildCustomFieldEntry(mapping, value));
            }
        }

        if (payload.getUtms() != null && !payload.getUtms().isEmpty()) {
            try {
                List<KommoField> leadCustomFields = kommoApiService.getLeadCustomFields();
                for (Map.Entry<String, String> utmEntry : payload.getUtms().entrySet()) {
                    String utmKey = utmEntry.getKey();
                    String utmValue = utmEntry.getValue();

                    if (utmValue == null || utmValue.isEmpty()) continue;

                    String cleanKey = utmKey.replace(" ", "_").toLowerCase();

                    KommoField matchedField = leadCustomFields.stream()
                            .filter(f -> {
                                String cleanField = f.getName().replace(" ", "_").toLowerCase();
                                return cleanField.equals(cleanKey) || cleanField.equals("utm_" + cleanKey);
                            })
                            .findFirst()
                            .orElse(null);

                    if (matchedField != null && !matchedField.isStandard()) {
                        customFields.add(Map.of(
                                "field_id", Integer.parseInt(matchedField.getId()),
                                "values", List.of(Map.of("value", utmValue))
                        ));
                    }
                }
            } catch (Exception e) {
                // Ignore errors fetching custom fields so we don't break lead creation
            }
        }

        lead.put("name", leadName);
        if (destination != null) {
            if (destination.getPipelineId() != null) lead.put("pipeline_id", destination.getPipelineId());
            if (destination.getStatusId() != null) lead.put("status_id", destination.getStatusId());
        }
        if (!customFields.isEmpty()) lead.put("custom_fields_values", customFields);
        return lead;
    }

    private Map<String, Object> buildContactPayload(FormPayload payload, List<FieldMapping> mappings) {
        Map<String, Object> contact = new HashMap<>();
        List<Map<String, Object>> customFields = new ArrayList<>();
        String contactName = payload.getName() != null ? payload.getName() : "Contato";

        for (FieldMapping mapping : mappings) {
            if (!"contact".equals(mapping.getKommoEntityType())) continue;

            String value = resolveStringValue(payload, mapping.getPayloadField());
            if (value == null) continue;

            if (mapping.isStandard() && "name".equals(mapping.getFieldCode())) {
                contactName = value;
            } else if (!mapping.isStandard()) {
                customFields.add(buildCustomFieldEntry(mapping, value));
            }
        }

        contact.put("name", contactName);
        if (!customFields.isEmpty()) contact.put("custom_fields_values", customFields);
        return contact;
    }

    /**
     * Builds a single {@code custom_fields_values} entry.
     * MULTITEXT fields (Phone, Email) require an {@code enum_code} alongside the value.
     */
    private Map<String, Object> buildCustomFieldEntry(FieldMapping mapping, String value) {
        Map<String, Object> fieldValue = new HashMap<>();
        fieldValue.put("value", value);

        if ("MULTITEXT".equals(mapping.getKommoFieldType())) {
            fieldValue.put("enum_code", "WORK");
        }

        return Map.of(
                "field_id", Integer.parseInt(mapping.getKommoFieldId()),
                "values", List.of(fieldValue)
        );
    }

    private String resolveStringValue(FormPayload payload, String payloadField) {
        Object raw = payload.getField(payloadField);
        return raw != null ? String.valueOf(raw) : null;
    }

    private FunnelDestination resolveFunnelDestination(IntegrationSettings settings, String direction) {
        if (settings == null) {
            return new FunnelDestination();
        }

        Map<String, FunnelDestination> directions = settings.getDirections();
        String normalizedDirection = normalizeDirection(direction);

        if (normalizedDirection != null && directions != null) {
            FunnelDestination destination = directions.get(normalizedDirection);
            if (hasDestination(destination)) {
                return destination;
            }
        }

        if (directions != null) {
            FunnelDestination defaultDestination = directions.get("mavi");
            if (hasDestination(defaultDestination)) {
                return defaultDestination;
            }
        }

        return FunnelDestination.builder()
                .pipelineId(settings.getPipelineId())
                .statusId(settings.getStatusId())
                .build();
    }

    private boolean hasDestination(FunnelDestination destination) {
        return destination != null
                && (destination.getPipelineId() != null || destination.getStatusId() != null);
    }

    private String normalizeDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return null;
        }
        return direction.trim().toLowerCase(Locale.ROOT);
    }
}
