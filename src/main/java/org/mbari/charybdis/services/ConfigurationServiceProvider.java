package org.mbari.charybdis.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Produces;
import org.mbari.vars.raziel.sdk.r1.ConfigurationService;
import org.mbari.vars.raziel.sdk.r1.RazielKiotaClient;

import java.net.URI;

@ApplicationScoped
public class ConfigurationServiceProvider {

    @Inject RazielConfig config;

    @Produces
    @ApplicationScoped
    ConfigurationService raziel() {
        var uri = URI.create(RazielConfig.adaptUrl(config.endpoint));
        Log.info("Connecting to Raziel at " + uri);
        return new RazielKiotaClient(uri);
    }

}
