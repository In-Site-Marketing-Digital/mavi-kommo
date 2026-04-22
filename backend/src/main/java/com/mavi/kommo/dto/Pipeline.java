package com.mavi.kommo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pipeline {
    private Integer id;
    private String name;
    private Boolean isMain;
    private List<PipelineStatus> statuses;
}
