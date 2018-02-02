package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.model.Version;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;

import static no.obos.util.servicebuilder.CdiModule.cdiModule;
import static no.obos.util.servicebuilder.ServiceConfig.serviceConfig;

public class TestService implements ServiceDefinition {

    public static final String PATH = "path";
    @Getter
    final Version version = new Version(1, 0, 0);


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload {
        String string;
        LocalDate date;
    }


    @Api
    public @Path(PATH)
    interface Resource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        Payload get();
    }


    public static class Impl implements Resource {
        @Override
        public Payload get() {
            return defaultPayload;
        }
    }


    public static Payload defaultPayload = new Payload("string", LocalDate.now());


    @Override
    public String getName() {
        return "test";
    }


    @Override
    public List<Class> getResources() {
        return Lists.newArrayList(Resource.class);
    }

    public final static TestService testService = new TestService();
    public final static ServiceConfig config = serviceConfig(testService)
            .cdi(props -> cdiModule
                    .bind(TestService.Impl.class, TestService.Resource.class)
            );
}
