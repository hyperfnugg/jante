package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.config.PropertyMap;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static java.util.stream.Collectors.toList;
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


    public static ServiceRunner serviceRunner(ServiceConfig serviceConfig) {
        return new ServiceRunner(serviceConfig, propertyMap, 8080);
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

        ServiceConfig runtimeConfig = initializeConfig(this.config, runtimeProperties);

        return this
                .withConfig(runtimeConfig)
                .withProperties(runtimeProperties)
                .withPort(runtimePort);
    }

    private Runtime startServer() {
        properties.failIfNotPresent(CONFIG_KEY_SERVER_CONTEXT_PATH);

        JerseyConfig jerseyConfig = new JerseyConfig(config.serviceDefinition);

        JettyServer.Configuration jettyConfig = JettyServer.Configuration.builder()
                .bindPort(port)
                .contextPath(properties.get(CONFIG_KEY_SERVER_CONTEXT_PATH))
                .build();
        JettyServer jettyServer = new JettyServer(jettyConfig, jerseyConfig);

        jerseyConfig
                .addRegistrators(config.registrators)
                .addBinders(config.binders);

        config.addons.forEach(it -> it.addToJerseyConfig(jerseyConfig));
        config.addons.forEach(it -> it.addToJettyServer(jettyServer));
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
        ServiceConfig configWithProperties = config.addPropertiesAndApplyToBindings(properties)
                .withAddons(ImmutableList.copyOf(config
                        .addons.stream()
                        .map(it -> it.withProperties(properties))
                        .collect(toList()
                        ))
                );
        return ServiceConfigInitializer.finalize(configWithProperties);
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
            runner.config.addons.forEach(addon -> {
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
