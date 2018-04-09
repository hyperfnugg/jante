package jante.exception;

import jante.model.HttpProblem;
import jante.model.LogLevel;
import lombok.Getter;
import jante.model.LogLevel;
import jante.model.HttpProblem;

import javax.ws.rs.WebApplicationException;

public class HttpProblemException extends WebApplicationException {
    @Getter
    private final HttpProblem httpProblem;

    @Getter
    private final LogLevel logLevel;

    @Getter
    private final boolean logStacktrace;

    public HttpProblemException(HttpProblem httpProblem, LogLevel logLevel, boolean logStacktrace) {
        super(httpProblem.title, httpProblem.status);
        this.httpProblem = httpProblem;
        this.logLevel = logLevel;
        this.logStacktrace = logStacktrace;
    }

    public HttpProblemException(HttpProblem httpProblem, Throwable cause, LogLevel logLevel, boolean logStacktrace) {
        super(httpProblem.title, cause, httpProblem.status);
        this.httpProblem = httpProblem;
        this.logLevel = logLevel;
        this.logStacktrace = logStacktrace;
    }
}
