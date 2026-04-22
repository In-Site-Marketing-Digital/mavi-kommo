package com.mavi.kommo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mavi.kommo.domain.KommoField;
import com.mavi.kommo.domain.KommoToken;
import com.mavi.kommo.exception.KommoApiException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class KommoApiService {

    private final OAuthService oAuthService;
    private final RestTemplate restTemplate;

    public KommoApiService(OAuthService oAuthService, RestTemplate restTemplate) {
        this.oAuthService = oAuthService;
        this.restTemplate = restTemplate;
    }

    public List<KommoField> getLeadCustomFields() {
        return fetchFields("leads", "lead");
    }

    public List<KommoField> getContactCustomFields() {
        return fetchFields("contacts", "contact");
    }

    public List<KommoField> getCompanyCustomFields() {
        return fetchFields("companies", "company");
    }

    public List<com.mavi.kommo.dto.Pipeline> getPipelines() {
        KommoToken token = oAuthService.getValidToken();
        String url = "https://" + token.getAccountDomain() + "/api/v4/leads/pipelines";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        List<com.mavi.kommo.dto.Pipeline> pipelines = new ArrayList<>();
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, JsonNode.class);

            JsonNode root = response.getBody();
            if (root != null && root.has("_embedded")) {
                JsonNode pipelinesNode = root.get("_embedded").get("pipelines");
                if (pipelinesNode != null && pipelinesNode.isArray()) {
                    for (JsonNode pNode : pipelinesNode) {
                        com.mavi.kommo.dto.Pipeline pipeline = new com.mavi.kommo.dto.Pipeline();
                        pipeline.setId(pNode.get("id").asInt());
                        pipeline.setName(pNode.get("name").asText());
                        pipeline.setIsMain(pNode.has("is_main") && pNode.get("is_main").asBoolean());
                        
                        List<com.mavi.kommo.dto.PipelineStatus> statuses = new ArrayList<>();
                        if (pNode.has("_embedded") && pNode.get("_embedded").has("statuses")) {
                            for (JsonNode sNode : pNode.get("_embedded").get("statuses")) {
                                com.mavi.kommo.dto.PipelineStatus status = new com.mavi.kommo.dto.PipelineStatus();
                                status.setId(sNode.get("id").asInt());
                                status.setName(sNode.get("name").asText());
                                status.setColor(sNode.has("color") ? sNode.get("color").asText() : "#ffffff");
                                statuses.add(status);
                            }
                        }
                        pipeline.setStatuses(statuses);
                        pipelines.add(pipeline);
                    }
                }
            }
        } catch (Exception e) {
            throw new KommoApiException("Failed to fetch pipelines: " + e.getMessage(), e);
        }
        return pipelines;
    }

    /**
     * Makes an authenticated POST to the Kommo REST API.
     *
     * @param path          path relative to the account domain (e.g. "/api/v4/leads/complex")
     * @param body          request payload
     * @param accessToken   bearer token
     * @param accountDomain the account subdomain (e.g. "mycompany.kommo.com")
     */
    public ResponseEntity<JsonNode> post(String path, Object body, String accessToken, String accountDomain) {
        String url = "https://" + accountDomain + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class);
        } catch (Exception e) {
            throw new KommoApiException("Kommo API call failed [POST " + path + "]: " + e.getMessage(), e);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private List<KommoField> fetchFields(String kommoEntity, String entityType) {
        KommoToken token = oAuthService.getValidToken();
        String url = "https://" + token.getAccountDomain()
                + "/api/v4/" + kommoEntity + "/custom_fields?limit=250";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        List<KommoField> fields = new ArrayList<>();
        fields.add(buildStandardNameField(entityType));

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, JsonNode.class);

            JsonNode root = response.getBody();
            if (root != null && root.has("_embedded")) {
                JsonNode customFields = root.get("_embedded").get("custom_fields");
                if (customFields != null && customFields.isArray()) {
                    for (JsonNode f : customFields) {
                        fields.add(KommoField.builder()
                                .id(f.get("id").asText())
                                .name(f.get("name").asText())
                                .fieldTypeCode(f.get("type").asInt())
                                .fieldType(resolveFieldTypeName(f.get("type").asInt()))
                                .entityType(entityType)
                                .isStandard(false)
                                .fieldCode(f.has("code") ? f.get("code").asText() : null)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            throw new KommoApiException(
                    "Failed to fetch fields for entity '" + kommoEntity + "': " + e.getMessage(), e);
        }

        return fields;
    }

    private KommoField buildStandardNameField(String entityType) {
        String label = switch (entityType) {
            case "lead"    -> "Nome do Lead";
            case "contact" -> "Nome do Contato";
            case "company" -> "Nome da Empresa";
            default        -> "Nome";
        };
        return KommoField.builder()
                .id("standard:" + entityType + ":name")
                .name(label)
                .fieldType("TEXT")
                .fieldTypeCode(0)
                .entityType(entityType)
                .isStandard(true)
                .fieldCode("name")
                .build();
    }

    private String resolveFieldTypeName(int typeCode) {
        return switch (typeCode) {
            case 1  -> "TEXT";
            case 2  -> "NUMERIC";
            case 3  -> "CHECKBOX";
            case 4  -> "SELECT";
            case 5  -> "MULTISELECT";
            case 6  -> "DATE";
            case 7  -> "URL";
            case 8  -> "MULTITEXT";   // Phone / Email
            case 9  -> "TEXTAREA";
            case 10 -> "RADIOBUTTON";
            case 13 -> "BIRTHDAY";
            case 15 -> "DATE_TIME";
            case 16 -> "PRICE";
            default -> "TEXT";
        };
    }
}
