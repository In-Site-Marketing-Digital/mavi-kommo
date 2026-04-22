package com.mavi.kommo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusResponse {

    private boolean connected;
    private String accountDomain;
    private Instant expiresAt;
}
