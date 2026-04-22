package com.mavi.kommo.service;

import com.mavi.kommo.domain.FieldMapping;
import com.mavi.kommo.repository.MappingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MappingService {

    private final MappingRepository mappingRepository;

    public MappingService(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public List<FieldMapping> getAllMappings() {
        return mappingRepository.findAll();
    }

    public List<FieldMapping> saveMappings(List<FieldMapping> mappings) {
        mappingRepository.saveAll(mappings);
        return mappingRepository.findAll();
    }
}
