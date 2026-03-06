package org.mbari.charybdis.services;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class RazielConfig {

    @ConfigProperty(name = "raziel.service.url")
    String endpoint;

    @ConfigProperty(name = "raziel.use-internal-urls")
    Boolean useInternalUrls;

    /**
     * Kiota needs the URL to be without the trailing "/v1" or "/config" suffix. THis is a HACK to adapt the URL
     * accordingly.
     *
     * @param url
     * @return
     */
    public static String adaptUrl(String url) {
        if (url.endsWith("/config")) {
            return url.substring(0, url.length() - 7);
        }
        else if (url.endsWith("/v1")) {
            return url.substring(0, url.length() - 3);
        }
        return url;
    }
}
