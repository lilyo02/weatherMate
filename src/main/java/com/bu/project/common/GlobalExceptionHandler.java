package com.bu.project.common;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.ConstraintViolationException;

//@RestControllerAdvice
public class GlobalExceptionHandler {

    public record Err(String code, String message, List<Map<String,String>> errors){}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Err handleBind(MethodArgumentNotValidException ex){
        var list = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
            .toList();
        return new Err("BAD_REQUEST","Validation failed", list);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Err handleConstraint(ConstraintViolationException ex){
        var list = ex.getConstraintViolations().stream()
            .map(v -> Map.of("field", v.getPropertyPath().toString(), "message", v.getMessage()))
            .toList();
        return new Err("BAD_REQUEST","Validation failed", list);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Err handleAny(Exception ex){
        return new Err("INTERNAL_ERROR", ex.getClass().getSimpleName(), List.of());
    }
}