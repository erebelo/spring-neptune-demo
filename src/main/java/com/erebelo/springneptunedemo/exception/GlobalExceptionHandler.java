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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ExceptionResponse handleException(Exception e) {
        return parseGeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ExceptionResponse handleIllegalStateException(IllegalStateException e) {
        return parseGeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleIllegalArgumentException(IllegalArgumentException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleConstraintViolationException(ConstraintViolationException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public @ResponseBody ExceptionResponse handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        return parseGeneralException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public @ResponseBody ExceptionResponse handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        String errorMessage = e.getMessage();
        String[] supportedHttpMethods = e.getSupportedMethods();
        if (!ObjectUtils.isEmpty(supportedHttpMethods)) {
            errorMessage += ". Supported methods: " + String.join(", ", supportedHttpMethods);
        }

        return parseGeneralException(HttpStatus.METHOD_NOT_ALLOWED, e, errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = null;
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            errorMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).toList().toString();
        }

        return parseGeneralException(HttpStatus.BAD_REQUEST, e, errorMessage);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleBadRequestException(BadRequestException e) {
        return parseGeneralException(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ExceptionResponse handleNotFoundException(NotFoundException e) {
        return parseGeneralException(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public @ResponseBody ExceptionResponse handleConflictException(ConflictException e) {
        return parseGeneralException(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public @ResponseBody ExceptionResponse handleUnprocessableEntityException(UnprocessableEntityException e) {
        return parseGeneralException(HttpStatus.UNPROCESSABLE_ENTITY, e);
    }

    private ExceptionResponse parseGeneralException(final HttpStatus httpStatus, final Exception e) {
        return parseGeneralException(httpStatus, e, e.getMessage());
    }

    private ExceptionResponse parseGeneralException(final HttpStatus httpStatus, final Exception e,
            final String message) {
        String errorMessage = ObjectUtils.isEmpty(message) ? "No defined message" : message;
        ExceptionResponse exceptionResponse = new ExceptionResponse(httpStatus, errorMessage,
                System.currentTimeMillis());

        log.error("Exception stack trace: {}" + System.lineSeparator() + "{}", exceptionResponse,
                ExceptionUtils.getStackTrace(e));
        return exceptionResponse;
    }
}
