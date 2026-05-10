package com.mavi.kommo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavi.kommo.config.StorageProperties;
import com.mavi.kommo.domain.FieldMapping;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JsonMappingRepository implements MappingRepository {

    private static final Logger log = LoggerFactory.getLogger(JsonMappingRepository.class);

    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;
    private File mappingsFile;

    public JsonMappingRepository(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        File dataDir = new File(storageProperties.getDataDir());
        if (!dataDir.exists()) {
            if (dataDir.mkdirs()) {
                log.info("Created data directory for mappings: {}", dataDir.getAbsolutePath());
            } else {
                log.warn("Could not create data directory for mappings: {}", dataDir.getAbsolutePath());
            }
        }
        this.mappingsFile = new File(dataDir, "mappings.json");
        log.info("Mapping repository initialized: file={}", mappingsFile.getAbsolutePath());
    }

    @Override
    public List<FieldMapping> findAll() {
        if (!mappingsFile.exists()) {
            log.info("Mappings file not found; returning empty mapping list");
            return new ArrayList<>();
        }
        try {
            List<FieldMapping> mappings = objectMapper.readValue(mappingsFile, new TypeReference<List<FieldMapping>>() {});
            log.info("Loaded mappings from disk: count={}", mappings.size());
            return mappings;
        } catch (IOException e) {
            log.error("Failed to read mappings from {}", mappingsFile.getAbsolutePath(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void saveAll(List<FieldMapping> mappings) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(mappingsFile, mappings);
            log.info("Saved mappings to disk: count={}, file={}", mappings.size(), mappingsFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to persist field mappings to {}", mappingsFile.getAbsolutePath(), e);
            throw new RuntimeException("Failed to persist field mappings", e);
        }
    }
}
