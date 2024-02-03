package thainguyen.controller.conf;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class CustomRestExceptionHandler
        extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<String> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST)
                .message("Method argument not valid")
                .errors(errors).build();
        return handleExceptionInternal(ex, responseComponent, headers, status, request);
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Object> handleConstraintViolation (ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " +
                    violation.getPropertyPath() + ": " + violation.getMessage());
        }

        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST)
                .message("Method argument not valid")
                .errors(errors)
                .build();
        return new ResponseEntity<>(responseComponent, new HttpHeaders(), responseComponent.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";
        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getLocalizedMessage())
                .errors(Arrays.asList(error))
                .build();
        return new ResponseEntity<>(responseComponent, new HttpHeaders(), responseComponent.getStatus());
    }


    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String error =
                ex.getName() + " should be of type " + ex.getRequiredType().getName();
        String errorMessage = "Failed to convert value of type '" + ex.getValue().getClass().getSimpleName() +
                "' to required type '" + ex.getRequiredType().getSimpleName() + "'";
        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST)
                .message(errorMessage)
                .errors(Arrays.asList(error))
                .build();
        return new ResponseEntity<>(responseComponent, new HttpHeaders(), responseComponent.getStatus());
    }




    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Object> handleSqlIntegrityConstraintViolation (SQLIntegrityConstraintViolationException ex,
                                                                         WebRequest request) {
        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.CONFLICT)
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(responseComponent, new HttpHeaders(), responseComponent.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                   HttpHeaders headers,
                                                                   HttpStatusCode status,
                                                                   WebRequest request) {
        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.NOT_FOUND)
                .message(error)
                .build();
        return new ResponseEntity<>(responseComponent, new HttpHeaders(), responseComponent.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatusCode status,
                                                                         WebRequest request) {
        StringBuilder error = new StringBuilder();
        error.append(ex.getMethod());
        error.append(" method is not supported for this request. Supported methods are ");
        Objects.requireNonNull(ex.getSupportedHttpMethods()).forEach(httpMethod -> error.append(httpMethod).append(" "));
        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .message(error.toString())
                .build();
        return new ResponseEntity<>(responseComponent, new HttpHeaders(), responseComponent.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatusCode status,
                                                                     WebRequest request) {
        StringBuilder error = new StringBuilder();
        error.append(ex.getContentType());
        error.append(" media type is not supported. Supported media types are ");
        Objects.requireNonNull(ex.getSupportedMediaTypes()).forEach(mediaType -> error.append(mediaType).append(" "));
        ResponseComponent<?> responseComponent = ResponseComponent.builder()
                .success(false)
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .message(error.toString())
                .errors(Arrays.asList(error.toString()))
                .message(ex.getLocalizedMessage())
                .build();
        return new ResponseEntity<>(responseComponent, new HttpHeaders(), responseComponent.getStatus());
    }


}