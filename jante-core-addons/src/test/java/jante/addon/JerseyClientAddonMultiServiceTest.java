package jante.addon;

import io.swagger.annotations.Api;
import jante.ServiceConfig;
import jante.TestServiceRunner;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;

import static jante.Injections.injections;
import static jante.ServiceConfig.serviceConfig;
import static jante.ServiceDefinitionUtil.stubServiceDefinition;
import static jante.TestServiceRunner.testServiceRunner;
import static jante.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static jante.addon.JerseyClientAddon.jerseyClientAddon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class JerseyClientAddonMultiServiceTest {
    Nested1 nestedMock1 = Mockito.mock(Nested1.class);
    Nested2 nestedMock2 = Mockito.mock(Nested2.class);


    @Test
    public void injection_of_clients_works() {
        TestServiceRunner.Runtime nestedRuntime1 = nestedRunner1.start().runtime;
        TestServiceRunner.Runtime nestedRuntime2 = nestedRunner2.start().runtime;
        try {


            testServiceRunner(
                    outerConfig
                            .addon(exceptionMapperAddon)
                            .addon(jerseyClientAddon(stubServiceDefinition(NESTED_NAME1, Nested1.class))
                                    .clientConfigBase(nestedRuntime1.clientConfig)
                                    .uri(nestedRuntime1.uri)
                            )
                            .addon(jerseyClientAddon(stubServiceDefinition(NESTED_NAME2, Nested2.class))
                                    .clientConfigBase(nestedRuntime2.clientConfig)
                                    .uri(nestedRuntime2.uri)
                            )
            ).oneShot(Outer.class, client -> {
                when(nestedMock1.get()).thenReturn("aaa");
                assertThat(client.get1()).isEqualTo("aaa");
                when(nestedMock2.doGet()).thenReturn("bbb");
                assertThat(client.get2()).isEqualTo("bbb");
                when(nestedMock1.get()).thenReturn("ccc");
                assertThat(client.target1()).isEqualTo("ccc");
                when(nestedMock2.doGet()).thenReturn("ddd");
                assertThat(client.target2()).isEqualTo("ddd");
                return "";
            });

        } finally {
            nestedRuntime1.stop();
            nestedRuntime2.stop();
        }
    }


    TestServiceRunner nestedRunner1 = testServiceRunner(
            serviceConfig(stubServiceDefinition(NESTED_NAME1, Nested1.class))
                    .addon(exceptionMapperAddon)
                    .inject(props -> injections
                            .bind(nestedMock1, Nested1.class)
                    )
    );
    TestServiceRunner nestedRunner2 = testServiceRunner(
            serviceConfig(stubServiceDefinition(NESTED_NAME2, Nested2.class))
                    .addon(exceptionMapperAddon)
                    .inject(props -> injections
                            .bind(nestedMock2, Nested2.class)
                    )
    );

    ServiceConfig outerConfig = serviceConfig(stubServiceDefinition("outer", Outer.class))
            .addon(exceptionMapperAddon)
            .inject(props -> injections
                    .bind(OuterImpl.class, Outer.class)
            );


    @Api
    @Path("r")
    public interface Outer {
        @GET
        @Path("a")
        @Produces("application/json")
        String get1();

        @GET
        @Path("b")
        @Produces("application/json")
        String get2();

        @GET
        @Path("c")
        @Produces("application/json")
        String target1();

        @GET
        @Path("d")
        @Produces("application/json")
        String target2();
    }


    public static class OuterImpl implements Outer {
        @Inject
        Nested1 nested1;
        @Inject
        Nested2 nested2;
        @Inject
        @Named(NESTED_NAME1)
        WebTarget target1;
        @Inject
        @Named(NESTED_NAME2)
        WebTarget target2;

        @Override
        public String get1() {
            return nested1.get();
        }

        @Override
        public String get2() {
            return nested2.doGet();
        }

        @Override
        public String target1() {
            return target1.path("banana").request().get().readEntity(String.class);
        }

        @Override
        public String target2() {
            return target2.path("phishy").request().get().readEntity(String.class);
        }
    }


    @Api
    @Path("banana")
    public interface Nested1 {
        @GET
        @Produces("application/json")
        String get();
    }


    @Api
    @Path("phishy")
    public interface Nested2 {
        @GET
        @Produces("application/json")
        String doGet();
    }


    static final String NESTED_NAME1 = "nested1";
    static final String NESTED_NAME2 = "nested2";
}

