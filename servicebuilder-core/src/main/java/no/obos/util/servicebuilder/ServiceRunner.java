package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.config.PropertyMap;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static lombok.AccessLevel.PRIVATE;
import static no.obos.util.servicebuilder.JettyServer.*;
import static no.obos.util.servicebuilder.config.PropertyMap.propertyMap;

import no.obos.util.servicebuilder.ServiceRunner.Runtime;


@Slf4j
@AllArgsConstructor
public class ServiceRunner {
    @Wither(PRIVATE)
    private final ServiceConfig config;
    @Wither(PRIVATE)
    final PropertyProvider properties;
    @Wither(PRIVATE)
    final int port;
    @Wither(PRIVATE)
    final String contextPath;


    public static ServiceRunner serviceRunner(ServiceConfig serviceConfig) {
        return new ServiceRunner(serviceConfig, propertyMap, 8080, null);
    }

    public ServiceRunner properties(PropertyProvider properties) {
        return withProperties(properties);
    }

    public Runtime start() {
        genericInitialization();

        return finalizeWithProperties()
                .startServer();
    }

    private ServiceRunner finalizeWithProperties() {
        PropertyProvider runtimeProperties =
                this.properties != null
                        ? this.properties
                        : PropertyMap.propertyFileFromJvmArgs();

        int runtimePort = runtimeProperties.getWithFallback(CONFIG_KEY_SERVER_PORT, port);

        String runtimeContextPath = properties.getWithFallback(CONFIG_KEY_SERVER_CONTEXT_PATH, contextPath);


        return this
                .withProperties(runtimeProperties)
                .withPort(runtimePort)
                .withContextPath(runtimeContextPath)
                ;
    }

    private Runtime startServer() {

        ServiceConfig.Runtime configRuntime = this.config.applyProperties(properties);

        JerseyConfig jerseyConfig = new JerseyConfig(configRuntime.serviceDefinition, configRuntime.cdiModules);

        JettyServer jettyConfig = jettyServer
                .bindPort(port)
                .contextPath(contextPath);

        for (Addon addon : configRuntime.addons.addons) {
            jettyConfig = addon.addToJettyServer(jettyConfig);
        }

        JettyServer.Runtime jettyRuntime = jettyConfig.start(jerseyConfig);
        return Runtime.builder()
                .configRuntime(configRuntime)
                .jerseyConfig(jerseyConfig)
                .jettyRuntime(jettyRuntime)
                .runner(this)
                .build();
    }


    private void genericInitialization() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }


    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class Runtime {
        public final ServiceConfig.Runtime configRuntime;
        public final JettyServer.Runtime jettyRuntime;
        public final JerseyConfig jerseyConfig;
        public ServiceRunner runner;

        public void join() {
            jettyRuntime.join();
        }

        public void stop() {
            configRuntime.addons.addons.forEach(addon -> {
                try {
                    addon.cleanUp();
                } catch (RuntimeException ex) {
                    log.error("Exception during cleanup", ex);
                }
            });
            jettyRuntime.stop();
        }

        private static RuntimeBuilder builder() {
            return new RuntimeBuilder();
        }

    }
}
