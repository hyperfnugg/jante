package jante.addon;

import jante.TestService.Resource;
import jante.TestServiceRunner;
import jante.exception.ExternalResourceException;
import jante.exception.ExternalResourceException.MetaData;
import jante.model.HttpProblem;
import jante.model.Version;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import java.net.URI;

import static jante.Injections.injections;
import static jante.ServiceConfig.serviceConfig;
import static jante.TestService.testService;
import static jante.TestServiceRunner.testServiceRunner;
import static jante.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static jante.client.ClientGenerator.clientGenerator;
import static jante.client.StubGenerator.stubGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JerseyClientAddonErrorHandlingTest {


    Resource resource = mock(Resource.class);
    TestServiceRunner runner = testServiceRunner(
            serviceConfig(testService)
                    .addon(exceptionMapperAddon
                            .stacktraceConfig(RuntimeException.class, false)
                    )
                    .inject(props -> injections
                            .bind(resource, Resource.class)
                    )
    );


    @Test
    public void error_handling_in_stub() {
        //given
        when(resource.get()).thenThrow(new RuntimeException("banan"));
        //when
        try {
            runner
                    .oneShot(Resource.class, Resource::get);
            Assert.fail();
        } catch (ExternalResourceException actual) {
            String incidentReferenceId = actual.getMetaData().httpResponseMetaData.httpProblem.incidentReferenceId;
            assertThat(incidentReferenceId).isNotEmpty();
            assertThat(actual.getMetaData()).isEqualToComparingFieldByFieldRecursively(
                    MetaData.builder()
                            .gotAnswer(true)
                            .httpRequestMetaData(ExternalResourceException.HttpRequestMetaData.builder()
                                    .url("http://localhost:0/path")
                                    .header("Accept", "application/json")
                                    .header("User-Agent", "Jersey/2.25.1 (Jersey InMemory Connector)")
                                    .build()
                            )
                            .httpResponseMetaData(ExternalResourceException.HttpResponseMetaData.builder()
                                    .httpProblem(HttpProblem.builder()
                                            .title("Internal Server Error")
                                            .detail("Det har oppstått en intern feil")
                                            .incidentReferenceId(incidentReferenceId)
                                            .status(500)
                                            .suggestedUserMessageInDetail(false)
                                            .build()
                                    )
                                    .incidentReferenceId(incidentReferenceId)
                                    .status(500)
                                    .header("Content-Length", "250")
                                    .header("Content-Type", "application/problem+json")
                                    .build()
                            )
                            .targetName("test")
                            .targetVersion(new Version(1, 0, 0))
            );
        }
    }

    @Test(expected = ProcessingException.class)
    public void no_custom_error_handling_when_call_fails_before_network() {
        Client client = clientGenerator.serviceDefinition(testService).generate();
        //given
        Resource resource = stubGenerator(client, URI.create("http://will.fail.badly")).generateClient(Resource.class);
        //when
        resource.get();
        Assert.fail();
    }
}
