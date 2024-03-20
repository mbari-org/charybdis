package org.mbari.charybdis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.NoopAuthService;
import org.mbari.vars.services.impl.vampiresquid.v1.VamService;
import org.mbari.vars.services.impl.vampiresquid.v1.VamWebServiceFactory;

@ApplicationScoped
public class MediaServiceProvider {

    @Inject
    MediaServiceConfig config;

    @Produces
    @ApplicationScoped
    MediaService mediaService() {
        var authService = new NoopAuthService();
        var serviceFactory = new VamWebServiceFactory(config.getEndpoint(), config.getTimeout());
        return new VamService(serviceFactory, authService);
    }
}
