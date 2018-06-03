package jante.addon;

import io.swagger.annotations.Api;
import jante.ServiceConfig;
import jante.TestService.Resource;
import jante.TestServiceRunner;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.LocalDate;

import static jante.Injections.injections;
import static jante.ServiceConfig.serviceConfig;
import static jante.ServiceDefinitionUtil.stubServiceDefinition;
import static jante.TestService.Payload;
import static jante.TestService.testService;
import static jante.TestServiceRunner.testServiceRunner;
import static jante.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static jante.addon.JerseyClientAddon.jerseyClientAddon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class JerseyClientAddonTest {
    Resource nestedResourceMock = Mockito.mock(Resource.class);


    @Test
    public void injection_of_client_works() {
        Payload expected = new Payload("eple", LocalDate.now().minusYears(1));

        when(nestedResourceMock.get()).thenReturn(expected);

        Payload actual =
                nestedRunner.oneShot((clientconfig, uri) -> testServiceRunner(
                        outerServiceConfig
                                .addon(jerseyClientAddon(testService)
                                        .clientConfigBase(clientconfig)
                                        .uri(uri)
                                )
                ).oneShot(OuterResource.class, OuterResource::get));
        assertThat(actual).isEqualTo(expected);
    }


    TestServiceRunner nestedRunner = testServiceRunner(
            serviceConfig(testService)
                    .inject(props -> injections
                            .bind(nestedResourceMock, Resource.class)
                    )
    );

    ServiceConfig outerServiceConfig = serviceConfig(stubServiceDefinition("outer", OuterResource.class))
            .addon(exceptionMapperAddon)
            .inject(props -> injections
                    .bind(OuterResourceImpl.class, OuterResource.class)
            );


    @Api
    @Path("kake")
    public interface OuterResource {
        @GET
        @Produces("application/json")
        Payload get();
    }


    public static class OuterResourceImpl implements OuterResource {
        @Inject
        Resource nestedResource;

        @Override
        public Payload get() {
            return nestedResource.get();
        }
    }

}

