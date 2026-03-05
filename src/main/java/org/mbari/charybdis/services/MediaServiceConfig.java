package org.mbari.charybdis.services;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@Singleton
public class MediaServiceConfig {

    @ConfigProperty(name = "media.service.timeout")
    Integer timeoutSeconds;

    private String normalizedEndpoint;
    private Duration timeout;

    public Duration getTimeout() {
        if (timeout == null) {
            timeout = Duration.ofSeconds(timeoutSeconds);
        }
        return timeout;
    }
}
