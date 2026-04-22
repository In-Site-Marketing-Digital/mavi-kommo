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
public class KommoField {

    /** Field ID — numeric string for custom, "standard:{entity}:{code}" for built-ins */
    private String id;

    /** Display name */
    private String name;

    /** Type label (TEXT, MULTITEXT, SELECT, CHECKBOX, …) */
    private String fieldType;

    /** Numeric type code returned by the Kommo API */
    private int fieldTypeCode;

    /** Kommo entity: "lead" | "contact" | "company" */
    private String entityType;

    @JsonProperty("isStandard")
    private boolean isStandard;

    /** PHONE, EMAIL, etc. — only set for multitext fields */
    private String fieldCode;
}
