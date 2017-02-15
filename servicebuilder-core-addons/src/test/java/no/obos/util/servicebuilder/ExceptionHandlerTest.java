package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.exception.UserMessageException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerTest {
    TestService.Resource testService = mock(TestService.Resource.class);
    ServiceConfig serviceConfig = ServiceConfig.builder()
            .serviceDefinition(TestService.instance)
            .bind(testService, TestService.Resource.class)
            .addon(ExceptionMapperAddon.builder().build())
            .build();

    @Test
    public void userMessageException() {
        //Given
        when(testService.get()).thenThrow(new UserMessageException("Boooom!", 421));

        //when
        Response response = TestServiceRunner.oneShot(serviceConfig, (clientconfig, uri) -> {
            return ClientBuilder.newClient(clientconfig).target(uri)
                    .path(TestService.PATH)
                    .request()
                    .get();
        });

        //then
        ProblemResponse actual = response.readEntity(ProblemResponse.class);
        assertThat(actual.detail).isEqualTo("Boooom!");
        assertThat(actual.status).isEqualTo(421);
        assertThat(actual.suggestedUserMessageInDetail).isEqualTo(true);
        assertThat(response.getStatus()).isEqualTo(421);
    }
}
