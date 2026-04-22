package com.mavi.kommo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

/**
 * Represents the fixed incoming payload sent by the form.
 * All fields are optional so the system degrades gracefully if a field is absent.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormPayload {

    private Map<String, Object> fields;
    private Map<String, String> utms;

    /**
     * Returns the value for a given payload field key.
     * Used by {@link com.mavi.kommo.service.LeadCreationService} when iterating mappings.
     */
    public Object getField(String fieldName) {
        if (fields == null) {
            return null;
        }
        return fields.get(fieldName);
    }

    /**
     * Helper to get the name directly if needed.
     */
    public String getName() {
        if (fields == null) return null;
        Object name = fields.get("name");
        return name != null ? String.valueOf(name) : null;
    }
}
