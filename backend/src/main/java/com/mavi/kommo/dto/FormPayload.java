package com.mavi.kommo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the fixed incoming payload sent by the form.
 * All fields are optional so the system degrades gracefully if a field is absent.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormPayload {

    private String name;
    private String email;
    private String phone;

    @JsonProperty("instagram_handle")
    private String instagramHandle;

    private String revenue;

    @JsonProperty("pt_investment")
    private String ptInvestment;

    @JsonProperty("clinic_owner")
    private Boolean clinicOwner;

    /**
     * Returns the value for a given payload field key.
     * Used by {@link com.mavi.kommo.service.LeadCreationService} when iterating mappings.
     */
    public Object getField(String fieldName) {
        return switch (fieldName) {
            case "name"             -> name;
            case "email"            -> email;
            case "phone"            -> phone;
            case "instagram_handle" -> instagramHandle;
            case "revenue"          -> revenue;
            case "pt_investment"    -> ptInvestment;
            case "clinic_owner"     -> clinicOwner;
            default                 -> null;
        };
    }
}
