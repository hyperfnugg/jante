package no.obos.util.servicebuilder.addon;

import com.google.common.base.Joiner;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.config.JerseyJaxrsConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.Version;
import org.eclipse.jetty.servlet.ServletHolder;

import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_API_PATHSPEC;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SwaggerAddon implements Addon {
    public final String pathSpec = "/swagger";

    @Wither(AccessLevel.PRIVATE)
    public final String contextPath;
    @Wither(AccessLevel.PRIVATE)
    public final String apiPath;
    @Wither(AccessLevel.PRIVATE)
    public final Version apiVersion;

    public static SwaggerAddon swaggerAddon = new SwaggerAddon(null, JettyServer.DEFAULT_API_PATH_SPEC, null);

    @Override
    public Addon withProperties(PropertyProvider properties) {
        return this
                .withContextPath(properties.getWithFallback(JettyServer.CONFIG_KEY_SERVER_CONTEXT_PATH, contextPath))
                .apiPath(properties.requireWithFallback(CONFIG_KEY_API_PATHSPEC, apiPath));
    }

    public Addon initialize(ServiceConfig serviceConfig) {
        return this
                .apiVersion(serviceConfig.serviceDefinition.getVersion());
    }


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator
                .register(ApiListingResource.class)
                .register(SwaggerSerializers.class)
        );
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ServletHolder apiDocServletHolder = new ServletHolder(new JerseyJaxrsConfig());
        apiDocServletHolder.setInitParameter("api.version", apiVersion.toString());
        String apiBasePath = Joiner.on('/')
                .join(contextPath, apiPath);
        apiDocServletHolder.setInitParameter("swagger.api.basepath", apiBasePath);
        apiDocServletHolder.setInitOrder(2);
        jettyServer.getServletContext().addServlet(apiDocServletHolder, pathSpec);
    }

    public SwaggerAddon contextPath(String contextPath) {
        return withContextPath(contextPath);
    }

    public SwaggerAddon apiPath(String apiPath) {
        return withApiPath(apiPath);
    }

    public SwaggerAddon apiVersion(Version apiVersion) {
        return withApiVersion(apiVersion);
    }
}
