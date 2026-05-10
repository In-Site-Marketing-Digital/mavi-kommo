package com.mavi.kommo.service;

import com.mavi.kommo.domain.FieldMapping;
import com.mavi.kommo.repository.MappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MappingService {

    private static final Logger log = LoggerFactory.getLogger(MappingService.class);

    private final MappingRepository mappingRepository;

    public MappingService(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public List<FieldMapping> getAllMappings() {
        List<FieldMapping> mappings = mappingRepository.findAll();
        log.info("Loaded field mappings from repository: count={}", mappings.size());
        return mappings;
    }

    public List<FieldMapping> saveMappings(List<FieldMapping> mappings) {
        log.info("Persisting field mappings: count={}", mappings.size());
        mappingRepository.saveAll(mappings);
        List<FieldMapping> saved = mappingRepository.findAll();
        log.info("Reloaded persisted field mappings: count={}", saved.size());
        return saved;
    }
}
