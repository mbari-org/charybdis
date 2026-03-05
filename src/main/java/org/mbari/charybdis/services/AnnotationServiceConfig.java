package org.mbari.charybdis.services;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@Singleton
public class AnnotationServiceConfig {

    @ConfigProperty(name = "annotation.service.timeout")
    Duration timeout;

    @ConfigProperty(name = "annotation.service.pagesize")
    Integer pageSize;

    public Duration getTimeout() {
        return timeout;
    }

    public Integer getPageSize() {
        return pageSize;
    }
}
