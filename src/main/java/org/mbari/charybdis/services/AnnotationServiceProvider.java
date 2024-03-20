package org.mbari.charybdis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.impl.annosaurus.v1.AnnosaurusHttpClient;


@ApplicationScoped
public class AnnotationServiceProvider {

    @Inject
    AnnotationServiceConfig config;

    @Produces
    @ApplicationScoped
    AnnotationService annotationService() {
        return new AnnosaurusHttpClient(config.getEndpoint(), config.getTimeout(), "");
    }
}
