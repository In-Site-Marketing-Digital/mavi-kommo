package com.mavi.kommo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavi.kommo.config.StorageProperties;
import com.mavi.kommo.domain.FieldMapping;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JsonMappingRepository implements MappingRepository {

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
            dataDir.mkdirs();
        }
        this.mappingsFile = new File(dataDir, "mappings.json");
    }

    @Override
    public List<FieldMapping> findAll() {
        if (!mappingsFile.exists()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(mappingsFile, new TypeReference<List<FieldMapping>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void saveAll(List<FieldMapping> mappings) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(mappingsFile, mappings);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist field mappings", e);
        }
    }
}
