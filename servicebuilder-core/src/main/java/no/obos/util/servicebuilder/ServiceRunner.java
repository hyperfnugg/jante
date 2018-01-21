package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.config.PropertyMap;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static lombok.AccessLevel.PRIVATE;
import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_CONTEXT_PATH;
import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_PORT;
import static no.obos.util.servicebuilder.config.PropertyMap.propertyMap;

@Slf4j
@AllArgsConstructor
public class ServiceRunner {
    @Wither(PRIVATE)
    final ServiceConfig config;
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

        ServiceConfig runtimeConfig = initializeConfig(this.config, runtimeProperties);

        return this
                .withConfig(runtimeConfig)
                .withProperties(runtimeProperties)
                .withPort(runtimePort)
                .withContextPath(runtimeContextPath)
                ;
    }

    private Runtime startServer() {
        JerseyConfig jerseyConfig = new JerseyConfig(config.serviceDefinition);

        JettyServer.Configuration jettyConfig = JettyServer.Configuration.builder()
                .bindPort(port)
                .contextPath(contextPath)
                .build();

        JettyServer jettyServer = new JettyServer(jettyConfig, jerseyConfig);

        jerseyConfig
                .addRegistrators(config.getRegistrators())
                .addBinders(config.getBindings());

        config.addons.addons.forEach(it -> it.addToJettyServer(jettyServer));

        jettyServer.start();
        return Runtime.builder()
                .jerseyConfig(jerseyConfig)
                .jettyConfig(jettyConfig)
                .jettyServer(jettyServer)
                .runner(this)
                .build();
    }


    private void genericInitialization() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static ServiceConfig initializeConfig(ServiceConfig config, final PropertyProvider properties) {
        ServiceConfig configWithProperties = config.applyProperties(properties);
        return ServiceConfigInitializer.initialize(configWithProperties);
    }


    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class Runtime {
        public final JettyServer jettyServer;
        public final JerseyConfig jerseyConfig;
        public JettyServer.Configuration jettyConfig;
        public ServiceRunner runner;

        public void join() {
            jettyServer.join();
        }

        public void stop() {
            runner.config.addons.addons.forEach(addon -> {
                try {
                    addon.cleanUp();
                } catch (RuntimeException ex) {
                    log.error("Exception during cleanup", ex);
                }
            });
            jettyServer.stop();
        }

        private static RuntimeBuilder builder() {
            return new RuntimeBuilder();
        }

    }
}
