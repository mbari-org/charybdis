/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.charybdis;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import io.helidon.config.Config;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.webserver.cors.CrossOriginConfig;
import org.mbari.charybdis.services.Annosaurus;
import org.mbari.charybdis.services.SimpleCountService;
import org.mbari.charybdis.services.SimpleQueryService;
import org.mbari.charybdis.services.Kakani2019Nature;
import org.mbari.charybdis.services.VampireSquid;

/**
 * The application main class.
 */
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() { }

    /**
     * Application main entry point.
     * @param args command line arguments.
     * @throws IOException if there are problems reading logging properties
     */
    public static void main(final String[] args) throws IOException {
        startServer();
    }

    /**
     * Start the server.
     * @return the created {@link WebServer} instance
     * @throws IOException if there are problems reading logging properties
     */
    static WebServer startServer() throws IOException {

        // load logging configuration
        setupLogging();

        // By default this will pick up application.yaml from the classpath
        var config = Config.create();

        WebServer server = WebServer.builder(createRouting(config))
                .config(config.get("server"))
                .addMediaSupport(JsonpSupport.create())
                .addMediaSupport(JsonbSupport.create())
                .build();

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        server.start()
            .thenAccept(ws -> {
                System.out.println(
                        "WEB server is up! http://localhost:" + ws.port() + "/n0");
                ws.whenShutdown().thenRun(()
                    -> System.out.println("WEB server is DOWN. Good bye!"));
                })
            .exceptionally(t -> {
                System.err.println("Startup failed: " + t.getMessage());
                t.printStackTrace(System.err);
                return null;
            });

        // Server threads are not daemon. No need to block. Just react.

        return server;
    }

    /**
     * Creates new {@link Routing}.
     *
     * @return routing configured with JSON support, a health check, and a service
     * @param config configuration of this server
     */
    private static Routing createRouting(Config config) {

        var metrics = MetricsSupport.create();
        var health = HealthSupport.builder()
                .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .build();
        var corsSupport = CorsSupport.builder()
                .addCrossOrigin(CrossOriginConfig.builder()
                        .allowOrigins("*")
                        .allowMethods("*")
                        .build())
                .addCrossOrigin(CrossOriginConfig.create())
                .build();

        // VARS Stuff
        var annosaurus = new Annosaurus(config);
        var vampireSquid = new VampireSquid(config);
        var n0 = new Kakani2019Nature(annosaurus, vampireSquid);
        var diveService = new SimpleQueryService(annosaurus, vampireSquid);
        var countService = new SimpleCountService(annosaurus, vampireSquid);


        return Routing.builder()
                .register(health)                   // Health at "/health"
                .register(metrics)                  // Metrics at "/metrics"
                .register("/n0", corsSupport, n0)
                .register("/query", corsSupport, diveService)
                .register("/count", corsSupport, countService)
                .build();
    }

    /**
     * Configure logging from logging.properties file.
     */
    private static void setupLogging() throws IOException {
        try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        }
    }

}
