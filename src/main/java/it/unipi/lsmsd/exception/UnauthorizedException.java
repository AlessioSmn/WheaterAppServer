package it.unipi.lsmsd.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// When this exception is thrown, Spring returns http 401 error code automatically
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) { super(message); }
    public UnauthorizedException(String message, Throwable cause) { super(message, cause); }
}
