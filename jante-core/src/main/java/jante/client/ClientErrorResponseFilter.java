package jante.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jante.exception.ExternalResourceException;
import jante.exception.ExternalResourceNotFoundException;
import jante.model.HttpProblem;
import jante.model.Version;
import jante.exception.ExternalResourceException;
import jante.exception.ExternalResourceException.HttpResponseMetaData;
import jante.exception.ExternalResourceException.MetaData;
import jante.exception.ExternalResourceNotFoundException;
import jante.model.HttpProblem;
import jante.model.Version;
import jante.util.FormatUtil;
import org.jvnet.hk2.annotations.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Priority(Priorities.AUTHENTICATION)
public class ClientErrorResponseFilter implements ClientResponseFilter {
    final ObjectMapper mapper;
    final String targetName;
    final Version targetVersion;

    @Inject
    public ClientErrorResponseFilter(
            ObjectMapper mapper,
            @Named(ClientGenerator.TARGET_NAME_INJECTION) @Optional String targetName,
            @Optional Version targetVersion) {
        this.mapper = mapper;
        this.targetName = targetName;
        this.targetVersion = targetVersion;
    }


    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
            throws IOException {
        // for non-200 response, deal with the custom error messages
        if (!Response.Status.Family.SUCCESSFUL.equals(responseContext.getStatusInfo().getFamily())) {
            ExternalResourceException.MetaData metaData = ExternalResourceException.MetaData.builder()
                    .httpRequestMetaData(getRequestMetaData(requestContext))
                    .httpResponseMetaData(getResponseMetaData(responseContext))
                    .gotAnswer(true)
                    .targetName(targetName)
                    .targetVersion(targetVersion)
                    .build();
            if (Response.Status.NOT_FOUND.getStatusCode() == responseContext.getStatus()) {
                throw new ExternalResourceNotFoundException(metaData);
            }
            throw new ExternalResourceException(metaData);
        }
    }

    private ExternalResourceException.HttpResponseMetaData getResponseMetaData(ClientResponseContext responseContext) throws IOException {
        Map<String, String> headers = FormatUtil.MultiMapAsStringMap(responseContext.getHeaders());
        ExternalResourceException.HttpResponseMetaData.HttpResponseMetaDataBuilder builder = ExternalResourceException.HttpResponseMetaData.builder()
                .status(responseContext.getStatus())
                .headers(headers);

        if (responseContext.hasEntity()) {
            String body;
            // setUp the "real" error message
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(responseContext.getEntityStream(), "UTF-8"))) {
                body = buffer.lines().collect(Collectors.joining("\n"));
            }
            try {
                HttpProblem problem = mapper.readValue(body, HttpProblem.class);
                if (problemWasParsed(problem)) {
                    builder.httpProblem(problem)
                            .incidentReferenceId(problem.incidentReferenceId);
                }
            } catch (JsonParseException | JsonMappingException e) {
                //ignore
            }

            if (builder.build().httpProblem == null) {
                builder.response(body);
            }
        }

        return builder.build();
    }

    private boolean problemWasParsed(HttpProblem problem) {
        return problem != null
                && problem.incidentReferenceId != null;
    }

    private ExternalResourceException.HttpRequestMetaData getRequestMetaData(ClientRequestContext requestContext) {
        Map<String, String> headers = FormatUtil.MultiMapAsStringMap(requestContext.getStringHeaders());
        return ExternalResourceException.HttpRequestMetaData.builder()
                .url(requestContext.getUri().toString())
                .headers(headers)
                .build();
    }
}
