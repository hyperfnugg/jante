package no.obos.util.servicebuilder;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;
import static no.obos.util.servicebuilder.util.ExceptionUtil.wrapCheckedExceptionsVoid;

@AllArgsConstructor
public final class JettyServer {

    public static final String CONFIG_KEY_SERVER_CONTEXT_PATH = "server.contextPath";
    public static final String CONFIG_KEY_SERVER_PORT = "server.port";
    public static final String CONFIG_KEY_API_PATHSPEC = "server.apiPath";

    public static final String DEFAULT_API_PATH_SPEC = "api";
    public static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    public static final int DEFAULT_BIND_PORT = 3000;


    public final String apiPathSpec;
    public final String bindAddress;
    @Wither(PRIVATE)
    public final String contextPath;
    @Wither(PRIVATE)
    public final int bindPort;
    @Wither(PRIVATE)
    final ImmutableList<Consumer<ServletContextHandler>> apiContextMutators;
    @Wither(PRIVATE)
    ImmutableList<Handler> handlers;

    public static JettyServer jettyServer = new JettyServer(
            "/" + DEFAULT_API_PATH_SPEC + "/*",
            DEFAULT_BIND_ADDRESS,
            "",
            DEFAULT_BIND_PORT,
            ImmutableList.of(),
            ImmutableList.of()
    );


    public Runtime start(JerseyConfig jerseyConfig) {
        ServletContainer sc = new ServletContainer(jerseyConfig.resourceConfig);

        ServletHolder servletHolder = new ServletHolder(sc);

        ServletContextHandler jerseyResourceContext = new ServletContextHandler();
        jerseyResourceContext.addServlet(servletHolder, apiPathSpec);
        jerseyResourceContext.setContextPath("/");

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(GuavaHelper.plus(this.handlers, jerseyResourceContext).reverse().toArray(new Handler[0]));

        Server server = new Server(InetSocketAddress.createUnresolved(bindAddress, bindPort));
        server.setHandler(contexts);
        wrapCheckedExceptionsVoid(server::start);
        return new Runtime(server, this);
    }

    public JettyServer addFilterToApi(Filter logFilter, ImmutableList<DispatcherType> dispatches) {

        return apiContextMutator(apiServletContext -> {
            String pathSpec = jettyServer.apiPathSpec;
            FilterHolder logFilterHolder = new FilterHolder(logFilter);
            apiServletContext
                    .addFilter(logFilterHolder, pathSpec, EnumSet.copyOf(dispatches));
        });
    }

    public JettyServer addStaticResources(String resourceUrlString, boolean hotReload, String pathSpec) {

        String path = Joiner.on('/')
                .skipNulls()
                .join(contextPath, pathSpec);
        WebAppContext webAppContext = new WebAppContext(resourceUrlString, path);
        if (hotReload) {
            webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        }
        return handler(webAppContext);
    }

    @AllArgsConstructor
    public static class Runtime {
        public final Server server;
        public final JettyServer jettyServer;

        public Runtime join() {
            wrapCheckedExceptionsVoid(server::join);
            return this;
        }

        public Runtime stop() {
            wrapCheckedExceptionsVoid(server::stop);
            return this;
        }

    }

    private JettyServer apiContextMutator(Consumer<ServletContextHandler> mutator) {
        return this.withApiContextMutators(GuavaHelper.plus(apiContextMutators, mutator));
    }

    private JettyServer handler(Handler handler) {
        return this.withHandlers(GuavaHelper.plus(handlers, handler));
    }

    public JettyServer contextPath(String contextPath) {
        return this.withContextPath(contextPath);
    }

    public JettyServer bindPort(int bindPort) {
        return this.withBindPort(bindPort);
    }
}
