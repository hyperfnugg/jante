package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static no.obos.util.servicebuilder.TestServiceRunnerJetty.testServiceRunnerJetty;
import static no.obos.util.servicebuilder.addon.CorsFilterAddon.corsFilterAddon;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ALLOW_CREDENTIALS;
import static org.assertj.core.api.Assertions.assertThat;

public class CorsFilterAddonTest {

    @Test
    public void tolerant_options() {

        ServiceConfig serviceConfig = TestService.config
                .addon(corsFilterAddon);
        Response call = testServiceRunnerJetty(serviceConfig)
                .property("server.port", "0")
                .oneShot(target -> target
                        .path("api")
                        .path(TestService.PATH)
                        .request()
                        .options()
                );
        MultivaluedMap<String, Object> headers = call.getHeaders();
        assertThat(headers.getFirst(ALLOW_CREDENTIALS)).isEqualTo("true");
    }

}
