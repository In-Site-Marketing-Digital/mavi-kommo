package com.mavi.kommo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldMapping {

    /** Payload field key (e.g. "revenue", "phone") */
    private String payloadField;

    /** Kommo field ID — numeric string for custom fields, or "standard:lead:name" for standard ones */
    private String kommoFieldId;

    /** Human-readable label shown in the UI */
    private String kommoFieldName;

    /** The Kommo entity this field belongs to: "lead" | "contact" | "company" */
    private String kommoEntityType;

    /** True when the field is a built-in Kommo field (e.g. lead name, contact name) */
    @JsonProperty("isStandard")
    private boolean isStandard;

    /** Kommo field type string (TEXT, MULTITEXT, SELECT, CHECKBOX, …) */
    private String kommoFieldType;

    /** Kommo field code — PHONE, EMAIL, etc. Used to identify standard multi-text fields */
    private String fieldCode;
}
