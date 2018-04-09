package jante.addon;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import jante.JettyServer;
import jante.log.ServerRequestIdFilter;
import jante.model.Addon;

import javax.servlet.DispatcherType;

/**
 * Legger til filtre for ObosLogFilter
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestIdAddon implements Addon {
    public static final ImmutableList<DispatcherType> DEFAULT_DISPATCHES = ImmutableList.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

    public static RequestIdAddon requestIdAddon = new RequestIdAddon();

    @Override
    public JettyServer addToJettyServer(JettyServer jettyServer) {
        ServerRequestIdFilter logFilter = new ServerRequestIdFilter();
        return jettyServer.addFilterToApi(logFilter, DEFAULT_DISPATCHES);
    }
}
