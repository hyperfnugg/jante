package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class UserMessageExceptionMapper implements ExceptionMapper<UserMessageException> {
    final private GenericExceptionHandler genericExceptionHandler;

    @Inject
    public UserMessageExceptionMapper(GenericExceptionHandler genericExceptionHandler) {
        this.genericExceptionHandler = genericExceptionHandler;
    }

    @Override
    public Response toResponse(UserMessageException exception) {
        return genericExceptionHandler.handle(exception, cfg -> cfg
                .status(exception.getResponse().getStatus())
                .logLevel(LogLevel.WARN)
                .detail(exception.getMessage())
                .userMessageInDetail(true)
                .logger(log)
        );
    }

}
