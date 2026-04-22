package com.mavi.kommo.dto;

import com.mavi.kommo.domain.KommoField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KommoFieldsResponse {

    private List<KommoField> lead;
    private List<KommoField> contact;
    private List<KommoField> company;
}
