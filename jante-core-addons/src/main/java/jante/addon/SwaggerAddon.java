package jante.addon;

import com.google.common.collect.ImmutableSet;
import io.swagger.config.Scanner;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerScannerLocator;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import jante.CdiModule;
import jante.JettyServer;
import jante.ServiceConfig;
import jante.model.Addon;
import jante.model.PropertyProvider;
import jante.model.Version;

import java.util.Set;
import java.util.stream.Collectors;

import static io.swagger.jaxrs.config.SwaggerContextService.CONFIG_ID_DEFAULT;
import static io.swagger.jaxrs.config.SwaggerContextService.SCANNER_ID_DEFAULT;
import static jante.CdiModule.cdiModule;
import static jante.JettyServer.CONFIG_KEY_API_PATHSPEC;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SwaggerAddon implements Addon {
    public final String pathSpec = "/swagger";

    @Wither(AccessLevel.PRIVATE)
    private final String apiPath;

    @Wither(AccessLevel.PRIVATE)
    private final Version apiVersion;

    @Wither(AccessLevel.PRIVATE)
    private final ImmutableSet<Class> resourceClasses;

    @Wither(AccessLevel.PRIVATE)
    private final ImmutableSet<String> schemes;

    @Wither(AccessLevel.PRIVATE)
    private final String host;


    public static SwaggerAddon swaggerAddon = new SwaggerAddon(JettyServer.DEFAULT_API_PATH_SPEC, null, ImmutableSet.of(), ImmutableSet.of("http"), null);

    @Override
    public Addon withProperties(PropertyProvider properties) {
        return this
                .apiPath(properties.requireWithFallback(CONFIG_KEY_API_PATHSPEC, apiPath));
    }

    @Override
    public Addon initialize(ServiceConfig.Runtime config) {
        return this
                .apiVersion(config.serviceDefinition.getVersion())
                .withResourceClasses(ImmutableSet.copyOf(config.serviceDefinition.getResources()))
                ;
    }


    @Override
    public CdiModule getCdiModule() {
        Scanner scanner = new Scanner() {
            @Override
            public Set<Class<?>> classes() {
                return resourceClasses.stream().map(it -> (Class<?>) it).collect(Collectors.toSet());
            }

            @Override
            public boolean getPrettyPrint() {
                return true;
            }

            @Override
            public void setPrettyPrint(boolean shouldPrettyPrint) {
            }
        };
        BeanConfig beanConfig = new BeanConfig();
        SwaggerScannerLocator.getInstance().putScanner(SCANNER_ID_DEFAULT, scanner);
        SwaggerConfigLocator.getInstance().putConfig(CONFIG_ID_DEFAULT, beanConfig);
        beanConfig.setPrettyPrint(true);
        beanConfig.setVersion(apiVersion.toString());
        beanConfig.setSchemes(schemes.toArray(new String[0]));
        beanConfig.setHost(host);
        beanConfig.setBasePath(apiPath);
        beanConfig.setScan(true);


        return cdiModule
                .register(ApiListingResource.class)
                .register(SwaggerSerializers.class)
                ;
    }

    public SwaggerAddon apiPath(String apiPath) {
        return withApiPath(apiPath);
    }

    public SwaggerAddon apiVersion(Version apiVersion) {
        return withApiVersion(apiVersion);
    }

    public SwaggerAddon schemes(String... schemes) {
        return withSchemes(ImmutableSet.copyOf(schemes));
    }

    public SwaggerAddon host(String host) {
        return withHost(host);
    }
}

