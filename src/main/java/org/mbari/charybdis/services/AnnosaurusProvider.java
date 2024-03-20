package org.mbari.charybdis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.mbari.vars.services.AnnotationService;

@ApplicationScoped
public class AnnosaurusProvider {

    @Inject
    AnnotationServiceConfig config;

    @Inject
    AnnotationService annotationService;

    @Produces
    @ApplicationScoped
    Annosaurus annosaurus() {
        return new Annosaurus(annotationService, config.getEndpoint(), config.getTimeout(), config.getPageSize());
    }
}
