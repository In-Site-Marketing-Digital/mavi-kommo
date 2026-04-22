package com.mavi.kommo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kommo")
public class KommoProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
