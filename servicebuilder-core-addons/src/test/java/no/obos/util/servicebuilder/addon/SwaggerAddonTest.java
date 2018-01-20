package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_API_PATHSPEC;
import static no.obos.util.servicebuilder.addon.SwaggerAddon.swaggerAddon;
import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerAddonTest extends AddonTestBase {

    @Test
    public void serves_swagger() {

        ServiceConfig serviceConfig = TestService.config
                .addon(swaggerAddon);
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
                .property(CONFIG_KEY_API_PATHSPEC, "ape")
                .oneShot(target -> target
                        .path("api")
                        .path("swagger.json")
                        .request()
                        .get()
                );
        //language=JSON
        String expected = "{\"swagger\":\"2.0\",\"info\":{\"version\":\"1.0.0\",\"title\":\"\"},\"basePath\":\"/test/v1.0/ape\",\"paths\":{\"/path\":{\"get\":{\"operationId\":\"get\",\"produces\":[\"application/json\"],\"parameters\":[],\"responses\":{\"200\":{\"description\":\"successful operation\",\"schema\":{\"$ref\":\"#/definitions/Payload\"},\"headers\":{}}}}}},\"definitions\":{\"Payload\":{\"type\":\"object\",\"properties\":{\"string\":{\"type\":\"string\"},\"date\":{\"type\":\"string\",\"format\":\"date\"}}}}}";
        assertThat(call.readEntity(String.class)).isEqualToIgnoringWhitespace(expected);
    }

}

