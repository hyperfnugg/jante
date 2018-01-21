package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.HttpProblem;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class HttpProblemExceptionMapper implements ExceptionMapper<HttpProblemException> {
    final private GenericExceptionHandler genericExceptionHandler;

    @Inject
    public HttpProblemExceptionMapper(GenericExceptionHandler genericExceptionHandler) {
        this.genericExceptionHandler = genericExceptionHandler;
    }

    @Override
    public Response toResponse(HttpProblemException exception) {

        return genericExceptionHandler.handle(exception, cfg -> {
                    HttpProblem problem = exception.getHttpProblem();
                    return cfg
                            .status(problem.status)
                            .detail(problem.detail)
                            .userMessageInDetail(problem.suggestedUserMessageInDetail)
                            .reference(problem.incidentReferenceId)
                            .title(problem.title)
                            .type(problem.type)
                            .context(problem.getContext())
                            .logStackTrace(exception.isLogStacktrace())
                            .logLevel(exception.getLogLevel())
                            .logger(log);
                }
        );
    }

}
