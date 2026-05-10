package com.mavi.kommo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mavi.kommo.config.StorageProperties;
import com.mavi.kommo.domain.KommoToken;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Repository
public class JsonTokenRepository implements TokenRepository {

    private static final Logger log = LoggerFactory.getLogger(JsonTokenRepository.class);

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
            if (dataDir.mkdirs()) {
                log.info("Created data directory for tokens: {}", dataDir.getAbsolutePath());
            } else {
                log.warn("Could not create data directory for tokens: {}", dataDir.getAbsolutePath());
            }
        }
        this.tokenFile = new File(dataDir, "token.json");
        log.info("Token repository initialized: file={}", tokenFile.getAbsolutePath());
    }

    @Override
    public Optional<KommoToken> findToken() {
        if (!tokenFile.exists()) {
            log.info("Token file not found");
            return Optional.empty();
        }
        try {
            KommoToken token = objectMapper.readValue(tokenFile, KommoToken.class);
            log.info("Loaded token from disk: account={}, expiresAt={}", token.getAccountDomain(), token.getExpiresAt());
            return Optional.of(token);
        } catch (IOException e) {
            log.error("Failed to read token from {}", tokenFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    @Override
    public void saveToken(KommoToken token) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tokenFile, token);
            log.info("Saved token to disk: account={}, expiresAt={}", token.getAccountDomain(), token.getExpiresAt());
        } catch (IOException e) {
            log.error("Failed to persist OAuth token to {}", tokenFile.getAbsolutePath(), e);
            throw new RuntimeException("Failed to persist OAuth token", e);
        }
    }

    @Override
    public void deleteToken() {
        if (tokenFile.exists()) {
            if (tokenFile.delete()) {
                log.info("Deleted token file: {}", tokenFile.getAbsolutePath());
            } else {
                log.warn("Could not delete token file: {}", tokenFile.getAbsolutePath());
            }
        } else {
            log.info("Token file already absent");
        }
    }
}
