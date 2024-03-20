package org.mbari.charybdis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@Singleton
public class MediaServiceConfig {

    @ConfigProperty(name = "media.service.url")
    String endpoint;

    @ConfigProperty(name = "media.service.timeout")
    Integer timeoutSeconds;

    private String normalizedEndpoint;
    private Duration timeout;

    public String getEndpoint() {
        if (normalizedEndpoint == null) {
            normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        }
        return normalizedEndpoint;
    }


    public Duration getTimeout() {
        if (timeout == null) {
            timeout = Duration.ofSeconds(timeoutSeconds);
        }
        return timeout;
    }
}
