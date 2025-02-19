package com.erebelo.springneptunedemo.exception;

import com.erebelo.springneptunedemo.exception.model.BadRequestException;
import com.erebelo.springneptunedemo.exception.model.ConflictException;
import com.erebelo.springneptunedemo.exception.model.NotFoundException;
import com.erebelo.springneptunedemo.exception.model.UnprocessableEntityException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        return parseGeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ResponseBody
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalStateException(IllegalStateException e) {
        return parseGeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        return parseGeneralException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e);
    }

    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        String errorMessage = e.getMessage();
        String[] supportedHttpMethods = e.getSupportedMethods();
        if (!ObjectUtils.isEmpty(supportedHttpMethods)) {
            errorMessage += ". Supported methods: " + String.join(", ", supportedHttpMethods);
        }

        return parseGeneralException(HttpStatus.METHOD_NOT_ALLOWED, e, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = null;
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            errorMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).toList().toString();
        }

        return parseGeneralException(HttpStatus.BAD_REQUEST, e, errorMessage);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e) {
        return parseGeneralException(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionResponse> handleConflictException(ConflictException e) {
        return parseGeneralException(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ExceptionResponse> handleUnprocessableEntityException(UnprocessableEntityException e) {
        return parseGeneralException(HttpStatus.UNPROCESSABLE_ENTITY, e);
    }

    private ResponseEntity<ExceptionResponse> parseGeneralException(final HttpStatus httpStatus, final Exception e) {
        return parseGeneralException(httpStatus, e, e.getMessage());
    }

    private ResponseEntity<ExceptionResponse> parseGeneralException(final HttpStatus httpStatus, final Exception e,
            final String message) {
        HttpStatus errorHttpStatus = ObjectUtils.isEmpty(httpStatus) ? HttpStatus.INTERNAL_SERVER_ERROR : httpStatus;
        String errorMessage = ObjectUtils.isEmpty(message) ? "No defined message" : message;
        ExceptionResponse exceptionResponse = new ExceptionResponse(errorHttpStatus, errorMessage,
                System.currentTimeMillis());

        log.error("Exception stack trace: {}" + System.lineSeparator() + "{}", exceptionResponse,
                ExceptionUtils.getStackTrace(e));
        return ResponseEntity.status(httpStatus).body(exceptionResponse);
    }
}
