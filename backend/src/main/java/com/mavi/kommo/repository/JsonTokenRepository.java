package com.mavi.kommo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mavi.kommo.config.StorageProperties;
import com.mavi.kommo.domain.KommoToken;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Repository
public class JsonTokenRepository implements TokenRepository {

    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;
    private File tokenFile;

    public JsonTokenRepository(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        File dataDir = new File(storageProperties.getDataDir());
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.tokenFile = new File(dataDir, "token.json");
    }

    @Override
    public Optional<KommoToken> findToken() {
        if (!tokenFile.exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(tokenFile, KommoToken.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void saveToken(KommoToken token) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tokenFile, token);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist OAuth token", e);
        }
    }

    @Override
    public void deleteToken() {
        if (tokenFile.exists()) {
            tokenFile.delete();
        }
    }
}
