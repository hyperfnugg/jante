package jante.addon;

import jante.ServiceConfig;
import jante.TestService;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static jante.TestServiceRunnerJetty.testServiceRunnerJetty;
import static jante.addon.CorsFilterAddon.corsFilterAddon;
import static jante.cors.ResponseCorsFilter.ALLOW_CREDENTIALS;
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
