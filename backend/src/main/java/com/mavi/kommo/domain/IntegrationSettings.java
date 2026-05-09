package com.mavi.kommo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationSettings {
    private Integer pipelineId;
    private Integer statusId;

    @Builder.Default
    private Map<String, FunnelDestination> directions = new HashMap<>();
}
