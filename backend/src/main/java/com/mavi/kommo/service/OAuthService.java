package com.mavi.kommo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mavi.kommo.config.KommoProperties;
import com.mavi.kommo.domain.KommoToken;
import com.mavi.kommo.exception.KommoApiException;
import com.mavi.kommo.exception.TokenNotFoundException;
import com.mavi.kommo.repository.TokenRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuthService {

    private final KommoProperties kommoProperties;
    private final TokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    public OAuthService(
            KommoProperties kommoProperties,
            TokenRepository tokenRepository,
            RestTemplate restTemplate) {
        this.kommoProperties = kommoProperties;
        this.tokenRepository = tokenRepository;
        this.restTemplate = restTemplate;
    }

    /** Builds the Kommo authorization URL the user should be redirected to. */
    public String buildAuthorizationUrl() {
        return String.format(
                "https://www.kommo.com/oauth?client_id=%s&state=%s&redirect_uri=%s&response_type=code",
                kommoProperties.getClientId(),
                UUID.randomUUID(),
                kommoProperties.getRedirectUri()
        );
    }

    /**
     * Exchanges the authorization code for an access token.
     *
     * @param code    the code received at the callback redirect
     * @param referer the account subdomain returned by Kommo (e.g. "mycompany.kommo.com")
     */
    public KommoToken exchangeCode(String code, String referer) {
        String tokenUrl = "https://" + referer + "/oauth2/access_token";

        Map<String, String> body = new HashMap<>();
        body.put("client_id", kommoProperties.getClientId());
        body.put("client_secret", kommoProperties.getClientSecret());
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", kommoProperties.getRedirectUri());

        HttpEntity<Map<String, String>> request = buildJsonRequest(body);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, JsonNode.class);
            KommoToken token = parseTokenResponse(response.getBody(), referer);
            tokenRepository.saveToken(token);
            return token;
        } catch (Exception e) {
            throw new KommoApiException("Failed to exchange OAuth code: " + e.getMessage(), e);
        }
    }

    /**
     * Uses the stored refresh token to obtain a new access token.
     * Persists the updated token automatically.
     */
    public KommoToken refreshToken(KommoToken expiredToken) {
        String tokenUrl = "https://" + expiredToken.getAccountDomain() + "/oauth2/access_token";

        Map<String, String> body = new HashMap<>();
        body.put("client_id", kommoProperties.getClientId());
        body.put("client_secret", kommoProperties.getClientSecret());
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", expiredToken.getRefreshToken());
        body.put("redirect_uri", kommoProperties.getRedirectUri());

        HttpEntity<Map<String, String>> request = buildJsonRequest(body);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, JsonNode.class);
            KommoToken newToken = parseTokenResponse(response.getBody(), expiredToken.getAccountDomain());
            tokenRepository.saveToken(newToken);
            return newToken;
        } catch (Exception e) {
            throw new KommoApiException("Failed to refresh OAuth token: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a valid (non-expired) token, refreshing automatically if needed.
     *
     * @throws TokenNotFoundException if no token is stored at all
     */
    public KommoToken getValidToken() {
        KommoToken token = tokenRepository.findToken()
                .orElseThrow(TokenNotFoundException::new);

        // Refresh if expiring within the next 60 seconds
        if (Instant.now().isAfter(token.getExpiresAt().minusSeconds(60))) {
            return refreshToken(token);
        }

        return token;
    }

    public boolean isConnected() {
        return tokenRepository.findToken().isPresent();
    }

    public Optional<KommoToken> getStoredToken() {
        return tokenRepository.findToken();
    }

    public void disconnect() {
        tokenRepository.deleteToken();
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private KommoToken parseTokenResponse(JsonNode body, String accountDomain) {
        if (body == null || !body.has("access_token")) {
            throw new KommoApiException("Invalid or empty token response from Kommo");
        }
        return KommoToken.builder()
                .accessToken(body.get("access_token").asText())
                .refreshToken(body.get("refresh_token").asText())
                .expiresAt(Instant.now().plusSeconds(body.get("expires_in").asInt()))
                .accountDomain(accountDomain)
                .build();
    }

    private <T> HttpEntity<T> buildJsonRequest(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
