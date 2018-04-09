package jante;

import jante.TestService.Payload;
import jante.TestService.Resource;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.ClientBuilder;
import java.time.LocalDate;

import static jante.CdiModule.cdiModule;
import static jante.ServiceConfig.serviceConfig;
import static jante.TestService.testService;
import static jante.TestServiceRunner.testServiceRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TestServiceRunnerTest {
    private Resource impl = Mockito.mock(Resource.class);
    private TestServiceRunner runner = testServiceRunner(
            serviceConfig(testService)
                    .cdi(props -> cdiModule.bind(impl, Resource.class))
    );

    @Test
    public void can_call_basic() {
        when(impl.get()).thenReturn(TestService.defaultPayload);
        String payload = runner.oneShot(((clientConfig, uri) ->

                        ClientBuilder.newClient(clientConfig).target(uri)
                                .path(TestService.PATH)
                                .request()
                                .get()
                                .readEntity(String.class)

                )
        );
        assertThat(payload).isEqualTo("{\n"
                + "  \"string\" : \"string\",\n"
                + "  \"date\" : \"" + LocalDate.now().toString() + "\"\n"
                + "}");
    }


    @Test
    public void can_call_target() {
        when(impl.get()).thenReturn(TestService.defaultPayload);
        String payload = runner.oneShot((target ->
                        target
                                .path(TestService.PATH)
                                .request()
                                .get()
                                .readEntity(String.class)

                )
        );
        assertThat(payload).isEqualTo("{\n"
                + "  \"string\" : \"string\",\n"
                + "  \"date\" : \"" + LocalDate.now().toString() + "\"\n"
                + "}");
    }


    @Test
    public void can_call_stub() {
        when(impl.get()).thenReturn(TestService.defaultPayload);
        Payload payload = runner
                .oneShot(Resource.class, (Resource::get));
        assertThat(payload).isEqualTo(TestService.defaultPayload);
    }

    @Test
    public void can_call_several_times() {
        Payload expected1 = new Payload("eple", LocalDate.now().minusYears(55));
        Payload expected2 = new Payload("banan", LocalDate.now().minusYears(66));

        TestServiceRunner.Runtime runtime = runner.start().runtime;

        when(impl.get()).thenReturn(expected1);
        Payload actual1 = runtime.call(Resource.class, Resource::get);

        when(impl.get()).thenReturn(expected2);
        Payload actual2 = runtime.call(Resource.class, Resource::get);
        runtime.stop();

        assertThat(actual1).isEqualTo(expected1);
        assertThat(actual2).isEqualTo(expected2);
    }
}

