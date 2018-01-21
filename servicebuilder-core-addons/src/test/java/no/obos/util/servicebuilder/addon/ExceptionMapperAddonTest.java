package no.obos.util.servicebuilder.addon;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.exception.HttpProblemException;
import no.obos.util.servicebuilder.exception.UserMessageException;
import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.model.LogLevel;
import no.obos.util.servicebuilder.util.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static no.obos.util.servicebuilder.CdiModule.cdiModule;
import static no.obos.util.servicebuilder.ServiceConfig.serviceConfig;
import static no.obos.util.servicebuilder.TestService.testService;
import static no.obos.util.servicebuilder.TestServiceRunner.testServiceRunner;
import static no.obos.util.servicebuilder.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionMapperAddonTest {
    TestService.Resource resource = mock(TestService.Resource.class);
    ServiceConfig config = serviceConfig(testService)
            .cdiModule(cdiModule
                    .bind(resource, TestService.Resource.class)
            )
            .addon(exceptionMapperAddon.stacktraceConfig(RuntimeException.class, false));
    TestServiceRunner runner = testServiceRunner(config);

    @Test
    public void userMessageException() throws IOException {
        //Given
        when(resource.get()).thenThrow(new UserMessageException("Boooom!", 421));

        //when
        Response response = runner.oneShot((clientconfig, uri) ->
                ClientBuilder.newClient(clientconfig)
                        .target(uri)
                        .path(TestService.PATH)
                        .request()
                        .get());

        //then
        String actualJson = response.readEntity(String.class);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper(config.serviceDefinition.getSerializationSpec());
        HttpProblem actual =
                objectMapper.readValue(actualJson, HttpProblem.class);
        assertThat(actual.detail).isEqualTo("Boooom!");
        assertThat(actual.status).isEqualTo(421);
        assertThat(actual.suggestedUserMessageInDetail).isEqualTo(true);
        assertThat(response.getStatus()).isEqualTo(421);
    }

    @Test
    public void httpProblemException() throws IOException {
        HttpProblem expected = HttpProblem.builder()
                .context("eple", "banan")
                .detail("farris")
                .status(599)
                .suggestedUserMessageInDetail(true)
                .title("fisk")
                .type("https://google.com")
                .build();

        //Given
        when(resource.get()).thenThrow(new HttpProblemException(expected, LogLevel.INFO, false));

        //when
        Response response = runner.oneShot((clientconfig, uri) ->
                ClientBuilder.newClient(clientconfig).target(uri)
                        .path(TestService.PATH)
                        .request()
                        .get());

        //then
        String actualJson = response.readEntity(String.class);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper(config.serviceDefinition.getSerializationSpec());
        HttpProblem actual =
                objectMapper.readValue(actualJson, HttpProblem.class);

        assertThat(actual.incidentReferenceId).isNotEmpty();
        assertThat(actual.toBuilder().incidentReferenceId(null).build()).isEqualToComparingFieldByFieldRecursively(expected);
    }
}

