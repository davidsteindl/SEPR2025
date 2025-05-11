package at.ac.tuwien.sepr.groupphase.backend.endpoint.exceptionhandler;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.LoginAttemptException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static at.ac.tuwien.sepr.groupphase.backend.config.SecurityConstants.MAX_LOGIN_TRIES;

/**
 * Register all your Java exceptions here to map them into meaningful HTTP
 * exceptions.
 * If you have special cases which are only important for specific endpoints,
 * use ResponseStatusExceptions
 * https://www.baeldung.com/exception-handling-for-rest-with-spring#responsestatusexception
 */

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail toProblemDetail(HttpStatus status, String message, ServletWebRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(URI.create(req.getRequest().getRequestURI()));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    /**
     * Use the @ExceptionHandler annotation to write handler for custom exceptions.
     */
    @ExceptionHandler({ NotFoundException.class, UsernameNotFoundException.class })
    public ResponseEntity<ProblemDetail> handleNotFound(RuntimeException ex, ServletWebRequest req) {
        LOG.warn("Not found: {}", ex.getMessage());
        ProblemDetail pd = toProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), req);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex, ServletWebRequest req) {
        LOG.warn("Bad credentials: {}", ex.getMessage());
        ProblemDetail pd = toProblemDetail(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ProblemDetail> handleLocked(LockedException ex, ServletWebRequest req) {
        LOG.warn("Locked: {}", ex.getMessage());
        ProblemDetail pd = toProblemDetail(HttpStatus.LOCKED, ex.getMessage(), req);
        return ResponseEntity.status(HttpStatus.LOCKED).body(pd);
    }

    @ExceptionHandler(LoginAttemptException.class)
    public ResponseEntity<ProblemDetail> handleLoginAttempt(LoginAttemptException ex, ServletWebRequest req) {
        int tries = ex.getLoginTries();
        HttpStatus status = tries >= MAX_LOGIN_TRIES
                ? HttpStatus.LOCKED
                : HttpStatus.UNAUTHORIZED;
        LOG.warn("Login attempt {} → status {}", tries, status);
        ProblemDetail pd = toProblemDetail(status, ex.getMessage(), req);
        pd.setProperty("loginTries", tries);
        return ResponseEntity.status(status).body(pd);
    }

    /**
     * Handles {@link ValidationException} by returning a 422 Unprocessable Entity response.
     *
     * @param e the validation exception
     * @return a {@link ValidationErrorRestDto} containing validation error details
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ValidationErrorRestDto handleValidationException(ValidationException e) {
        LOG.warn("Terminating request processing with status 422 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
        return new ValidationErrorRestDto(e.summary(), e.errors());
    }


    /**
     * Override methods from ResponseEntityExceptionHandler to send a customized
     * HTTP response for a know exception
     * from e.g. Spring
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return ResponseEntity
                .status(statusCode.value())
                .headers(headers)
                .body(errors);
    }
}
