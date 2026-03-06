package org.mbari.charybdis.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.mbari.vars.annosaurus.sdk.r1.AnnosaurusHttpClient;
import org.mbari.vars.raziel.sdk.r1.ConfigurationService;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class AnnosaurusProvider {

    public static final String ENDPOINT_NAME = "annosaurus";

    @Inject
    ConfigurationService configurationService;

    @Inject
    RazielConfig razielConfig;

    @Inject
    AnnotationServiceConfig config;

    @Produces
    @ApplicationScoped
    Annosaurus annosaurus() throws Exception {
        var timeout = config.getTimeout();
        var pageSize = config.getPageSize();
        var useInternalUrls = razielConfig.useInternalUrls;
        return configurationService.endpoints(useInternalUrls)
                .thenApply(endpoints -> endpoints.stream()
                        .filter(e -> ENDPOINT_NAME.equalsIgnoreCase(e.name()))
                        .peek(e -> Log.info(ENDPOINT_NAME + " endpoint: " + e.url()))
                        .findFirst()
                        .map(config -> new AnnosaurusHttpClient(config.url(), timeout, ""))
                        .map(client -> new Annosaurus(client, timeout, pageSize))
                        .orElseThrow(() -> new IllegalStateException("No Annosaurus endpoint found")))
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);

    }
}
