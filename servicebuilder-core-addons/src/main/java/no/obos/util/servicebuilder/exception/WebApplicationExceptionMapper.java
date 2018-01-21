package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    final private GenericExceptionHandler genericExceptionHandler;

    @Inject
    public WebApplicationExceptionMapper(GenericExceptionHandler genericExceptionHandler) {
        this.genericExceptionHandler = genericExceptionHandler;
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        return genericExceptionHandler.handle(exception, cfg -> cfg
                .status(exception.getResponse().getStatus())
                .logLevel(LogLevel.WARN)
                .logger(log)
        );
    }

}
