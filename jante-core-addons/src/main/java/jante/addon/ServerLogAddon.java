package jante.addon;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import jante.CdiModule;
import jante.log.ServerLogFilter;
import jante.log.ServerLogger;
import jante.log.model.LogParams;
import jante.model.Addon;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.function.Predicate;

import static jante.CdiModule.cdiModule;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerLogAddon implements Addon {

    public final ImmutableList<Predicate<ContainerRequestContext>> fastTrackFilters =
            ImmutableList.of(
                    request -> "OPTIONS".equals(request.getMethod()),
                    request -> request.getUriInfo() != null
                            && request.getUriInfo().getAbsolutePath().toString().contains("swagger.json"),
                    request -> request.getUriInfo() != null
                            && request.getUriInfo().getAbsolutePath().toString().contains("metrics")
            );

    @Wither(AccessLevel.PRIVATE)
    public final LogParams logParams;

    public static final ServerLogAddon serverLogAddon = new ServerLogAddon(LogParams.defaults);


    @Override
    public CdiModule getCdiModule() {
        ServerLogger serverLogger = new ServerLogger(fastTrackFilters, logParams);
        return cdiModule
                .bind(serverLogger, ServerLogger.class)
                .register(ServerLogFilter.class);
    }

    public ServerLogAddon logParams(LogParams logParams) {
        return withLogParams(logParams);
    }
}
