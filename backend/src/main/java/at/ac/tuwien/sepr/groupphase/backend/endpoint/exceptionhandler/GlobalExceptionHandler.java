package at.ac.tuwien.sepr.groupphase.backend.endpoint.exceptionhandler;

import at.ac.tuwien.sepr.groupphase.backend.exception.LoginAttemptException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
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
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

import static at.ac.tuwien.sepr.groupphase.backend.config.SecurityConstants.MAX_LOGIN_TRIES;

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
        HttpStatus status = ex.getLoginTries() >= MAX_LOGIN_TRIES
                ? HttpStatus.LOCKED
                : HttpStatus.UNAUTHORIZED;

        LOG.warn("Login attempt {} → status {}", ex.getLoginTries(), status);
        ProblemDetail pd = toProblemDetail(status, ex.getMessage(), req);
        pd.setProperty("loginTries", ex.getLoginTries());
        return ResponseEntity.status(status).body(pd);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        ServletWebRequest req = (ServletWebRequest) request;

        String summary = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail pd = toProblemDetail(HttpStatus.BAD_REQUEST, "Validation failed: " + summary, req);
        pd.setProperty("fieldErrors", ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage)));

        return ResponseEntity
                .status(statusCode.value())
                .headers(headers)
                .body(pd);
    }
}
