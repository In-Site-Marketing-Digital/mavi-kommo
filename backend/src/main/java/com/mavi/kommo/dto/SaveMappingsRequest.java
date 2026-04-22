package com.mavi.kommo.dto;

import com.mavi.kommo.domain.FieldMapping;
import lombok.Data;

import java.util.List;

@Data
public class SaveMappingsRequest {

    private List<FieldMapping> mappings;
}
