package no.obos.util.servicebuilder.addon;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.CdiModule;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.util.ApiVersionUtil;
import no.obos.util.servicebuilder.util.ObosHealthCheckRegistry;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.client.ClientConfig;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

import static no.obos.util.servicebuilder.CdiModule.cdiModule;
import static no.obos.util.servicebuilder.client.ClientGenerator.clientGenerator;
import static no.obos.util.servicebuilder.client.StubGenerator.stubGenerator;
import static no.obos.util.servicebuilder.client.TargetGenerator.targetGenerator;

/**
 * Genererer klienter for en service med jersey klient-api og binder dem til context.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JerseyClientAddon implements Addon {

    public static final String CONFIG_KEY_URL = "service.url";

    public final ServiceDefinition serviceDefinition;
    @Wither(AccessLevel.PRIVATE)
    public final URI uri;
    @Wither(AccessLevel.PRIVATE)
    public final ClientConfig clientConfigBase;
    @Wither(AccessLevel.PRIVATE)
    public final boolean targetThrowsExceptions;
    @Wither(AccessLevel.PRIVATE)
    public final Runtime runtime;

    public static JerseyClientAddon jerseyClientAddon(ServiceDefinition serviceDefinition) {
        return new JerseyClientAddon(serviceDefinition, null, null, true, null);
    }


    @Override
    public Addon withProperties(PropertyProvider properties) {
        String name = serviceDefinition.getName();
        String prefix = name + ".";

        String url = properties.requireWithFallback(prefix + CONFIG_KEY_URL, uri == null ? null : uri.toString());
        URI uri = URI.create(url);
        return this
                .uri(uri);
    }

    @Override
    public Addon initialize(ServiceConfig.Runtime config) {
        String clientAppName = config.serviceDefinition.getName()
                + ":"
                + ApiVersionUtil.getApiVersion(config.serviceDefinition.getClass());
        Client client = clientGenerator.serviceDefinition(serviceDefinition)
                .clientConfigBase(clientConfigBase)
                .clientAppName(clientAppName)
                .generate();
        StubGenerator stubGenerator = stubGenerator(client, uri);

        TargetGenerator targetGenerator = targetGenerator(client, uri)
                .throwExceptionForErrors(true);

        return withRuntime(new Runtime(client, stubGenerator, targetGenerator));
    }


    @Override
    public CdiModule getCdiModule() {
        CdiModule ret = cdiModule;

        String serviceName = serviceDefinition.getName();
        if (!Strings.isNullOrEmpty(serviceName)) {
            ret = ret
                    .bindNamed(this, JerseyClientAddon.class, serviceName)
                    .bindNamed(runtime.client, Client.class, serviceName)
                    .bind(binder -> binder.bindFactory(new WebTargetFactory(runtime.targetGenerator)).to(WebTarget.class).named(serviceName))
                    .bindNamed(runtime.stubGenerator, StubGenerator.class, serviceName);
        } else {
            ret = ret
                    .bind(this, JerseyClientAddon.class)
                    .bind(runtime.client, Client.class)
                    .bindFactory(new WebTargetFactory(runtime.targetGenerator), WebTarget.class)
                    .bind(runtime.stubGenerator, StubGenerator.class);
        }

        for (Class clazz : serviceDefinition.getResources()) {
            //noinspection unchecked
            ret = ret
                    .bind(binder -> binder
                            .bindFactory(new StubFactory(clazz, runtime.stubGenerator)).to(clazz).in(Singleton.class)
                    );
        }
        return ret;
    }

    @Override
    public JettyServer addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerPingCheck(serviceDefinition.getName() + ": " + uri.toString(), uri.toString());
        return jettyServer;
    }


    @AllArgsConstructor
    public static class StubFactory implements Factory<Object> {
        final Class<?> requiredType;
        final StubGenerator generator;

        public Object provide() {
            return generator
                    .generateClient(requiredType);
        }

        @Override
        public void dispose(Object instance) {

        }
    }


    @AllArgsConstructor
    public static class WebTargetFactory implements Factory<WebTarget> {
        TargetGenerator generator;

        public WebTarget provide() {
            return generator.generate();
        }

        @Override
        public void dispose(WebTarget instance) {
        }
    }


    @AllArgsConstructor
    public static class Runtime {
        public final Client client;
        public final StubGenerator stubGenerator;
        public final TargetGenerator targetGenerator;
    }


    public JerseyClientAddon uri(URI uri) {
        return withUri(uri);
    }

    public JerseyClientAddon clientConfigBase(ClientConfig clientConfigBase) {
        return withClientConfigBase(clientConfigBase);
    }

    public JerseyClientAddon targetThrowsExceptions(boolean targetThrowsExceptions) {
        return withTargetThrowsExceptions(targetThrowsExceptions);
    }
}
