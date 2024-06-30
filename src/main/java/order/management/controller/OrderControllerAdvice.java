package order.management.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import order.management.dto.ErrorDto;
import order.management.exception.InsufficientQuantityException;
import order.management.exception.OrderNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class OrderControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        log.warn(ex.getLocalizedMessage(), ex);
        Optional<ObjectError> error = ex.getAllErrors().stream().findFirst();
        return ResponseEntity.badRequest().body(
                ErrorDto.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(error.isPresent() ? error.get().getDefaultMessage() : "")
                        .error(ex.getLocalizedMessage())
                        .path(request.getDescription(false))
                        .build()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ErrorDto handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        log.error(ex.getLocalizedMessage(), ex);
        return buildErrorResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OrderNotFoundException.class)
    protected ErrorDto handleOrderNotFoundException(OrderNotFoundException ex, WebRequest request) {
        log.warn(ex.getLocalizedMessage(), ex);
        return buildErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EntityNotFoundException.class)
    protected ErrorDto handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        log.warn(ex.getLocalizedMessage(), ex);
        return buildErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InsufficientQuantityException.class)
    protected ErrorDto handleInsufficientQuantityException(InsufficientQuantityException ex, WebRequest request) {
        log.warn(ex.getLocalizedMessage(), ex);
        return buildErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    private ErrorDto buildErrorResponse(RuntimeException ex, WebRequest request, HttpStatus status){
        return ErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .error(status.getReasonPhrase())
                .status(status.value())
                .message(ex.getLocalizedMessage())
                .path(request.getDescription(false))
                .build();
    }
}
