package com.mavi.kommo.controller;

import com.mavi.kommo.domain.KommoToken;
import com.mavi.kommo.dto.AuthStatusResponse;
import com.mavi.kommo.repository.TokenRepository;
import com.mavi.kommo.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final OAuthService oAuthService;
    private final TokenRepository tokenRepository;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public AuthController(OAuthService oAuthService, TokenRepository tokenRepository) {
        this.oAuthService = oAuthService;
        this.tokenRepository = tokenRepository;
    }

    /** Step 1 — redirect the user to Kommo's OAuth consent screen. */
    @GetMapping("/kommo")
    public void initiateAuth(HttpServletResponse response) throws IOException {
        log.info("Starting Kommo OAuth authorization flow");
        response.sendRedirect(oAuthService.buildAuthorizationUrl());
    }

    /**
     * Step 2 — Kommo redirects here with {code, referer, state}.
     * We exchange the code, persist the token, then redirect back to the frontend.
     */
    @GetMapping("/kommo/callback")
    public void handleCallback(
            @RequestParam String code,
            @RequestParam String referer,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {

        log.info("Handling Kommo OAuth callback: referer={}, statePresent={}", referer, state != null);
        oAuthService.exchangeCode(code, referer);
        log.info("Kommo OAuth callback completed for account={}", referer);
        response.sendRedirect(frontendUrl + "/?connected=true");
    }

    @GetMapping("/status")
    public ResponseEntity<AuthStatusResponse> getStatus() {
        log.info("Checking Kommo OAuth status");
        Optional<KommoToken> tokenOpt = oAuthService.getStoredToken();

        if (tokenOpt.isEmpty()) {
            log.info("Kommo OAuth status: disconnected");
            return ResponseEntity.ok(AuthStatusResponse.builder().connected(false).build());
        }

        KommoToken token = tokenOpt.get();
        log.info("Kommo OAuth status: connected account={}", token.getAccountDomain());
        return ResponseEntity.ok(AuthStatusResponse.builder()
                .connected(true)
                .accountDomain(token.getAccountDomain())
                .expiresAt(token.getExpiresAt())
                .build());
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect() {
        log.info("Disconnecting Kommo OAuth account");
        oAuthService.disconnect();
        return ResponseEntity.ok().build();
    }
}
