package org.mbari.charybdis.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.mbari.vars.raziel.sdk.r1.ConfigurationService;
import org.mbari.vars.vampiresquid.sdk.r1.VampireSquidKiotaClient;

import java.net.URI;
import java.util.concurrent.TimeUnit;


@ApplicationScoped
public class VampireSquidProvider {

    public static final String ENDPOINT_NAME = "vampire-squid";

    @Inject
    ConfigurationService configurationService;

    @Inject
    MediaServiceConfig config;

    @Produces
    @ApplicationScoped
    VampireSquid vampireSquid() throws Exception {
        var timeout = config.getTimeout();
        return configurationService.endpoints()
                .thenApply(endpoints -> endpoints.stream()
                        .filter(e -> ENDPOINT_NAME.equalsIgnoreCase(e.name()))
                        .peek(e -> Log.info(ENDPOINT_NAME + " endpoint: " + e.url()))
                        .findFirst()
                        .map(config -> {
                            if (config.url().startsWith("http://")) {
                                Log.warn(ENDPOINT_NAME + " endpoint is using http. " +
                                        "This is not recommended. " +
                                        "Please use https instead");
                            }
                            var uri = URI.create(RazielConfig.adaptUrl(config.url()));
                            Log.info("Connecting to Vampire Squid at " + uri);
                            var client = new VampireSquidKiotaClient(uri);
                            return new VampireSquid(client, timeout);
                        })
                        .orElseThrow(() -> new IllegalStateException("No Vampire Squid endpoint found")))
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
