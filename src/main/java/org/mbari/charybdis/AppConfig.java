package org.mbari.charybdis;

import org.eclipse.microprofile.config.ConfigProvider;

public class AppConfig {

    public static final String NAME = "charybdis";

    public static final String VERSION = ConfigProvider.getConfig()
            .getOptionalValue("quarkus.application.version", String.class)
            .orElse("unknown");

    public static final String DESCRIPTION = "Annotation and Media Services for MBARI";

}
