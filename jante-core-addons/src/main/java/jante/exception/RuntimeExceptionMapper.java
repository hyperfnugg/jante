package jante.exception;

import lombok.extern.slf4j.Slf4j;
import jante.model.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Slf4j
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    final private GenericExceptionHandler genericExceptionHandler;

    @Inject
    public RuntimeExceptionMapper(GenericExceptionHandler genericExceptionHandler) {
        this.genericExceptionHandler = genericExceptionHandler;
    }

    @Override
    public Response toResponse(RuntimeException exception) {
        return genericExceptionHandler.handle(exception, cfg -> cfg
                .status(INTERNAL_SERVER_ERROR.getStatusCode())
                .logLevel(LogLevel.ERROR)
                .detail("Det har oppstått en intern feil")
                .logger(log)
        );
    }

}
