package com.mavi.kommo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class KommoIntegratorApplication {

    private static final Logger log = LoggerFactory.getLogger(KommoIntegratorApplication.class);

    public static void main(String[] args) {
        log.info("Starting Mavi Kommo Integrator backend");
        SpringApplication.run(KommoIntegratorApplication.class, args);
    }
}
