package com.food.ordering.system.application.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleException(Exception exception){
        log.error(exception.getMessage(),exception);

        return ErrorDto.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Unexpected Error!")
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleException(ValidationException validationException){
        ErrorDto errorDto;

        if (validationException instanceof ConstraintViolationException){
            String violations = extractViolationsFromException((ConstraintViolationException) validationException);
            log.error(violations,validationException);
            errorDto = ErrorDto.builder()
                    .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(violations)
                    .build();
        }else {
            String exceptionMessage = validationException.getMessage();
            log.error(exceptionMessage,validationException);
            errorDto = ErrorDto.builder()
                    .code(HttpStatus.GONE.getReasonPhrase())
                    .message(exceptionMessage)
                    .build();
        }


        return errorDto;
    }

    private String extractViolationsFromException(ConstraintViolationException validationException) {
        return validationException
                .getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("--"));
    }

}
