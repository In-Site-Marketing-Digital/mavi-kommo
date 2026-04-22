package com.mavi.kommo.repository;

import com.mavi.kommo.domain.FieldMapping;

import java.util.List;

public interface MappingRepository {

    List<FieldMapping> findAll();

    void saveAll(List<FieldMapping> mappings);
}
