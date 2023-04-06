package com.github.Aseeef;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;

public class ReflectiveAseefianException extends RuntimeException {

    @Getter
    private ExceptionType exceptionType;

    public ReflectiveAseefianException(Throwable cause, ExceptionType exceptionType) {
        super(cause);
        this.exceptionType = exceptionType;
    }

    public ReflectiveAseefianException(String message, ExceptionType exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public ReflectiveAseefianException(Throwable cause) {
        super(cause);
        this.exceptionType = exceptionType;
    }

    public ReflectiveAseefianException(String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    private ExceptionType deduceExceptionType(Throwable throwable) {
        if (throwable instanceof NoSuchFieldException) {
            return ExceptionType.FIELD_NOT_FOUND;
        } else if (throwable instanceof InvocationTargetException) {
            return ExceptionType.INVOCATION_EXCEPTION;
        } else if (throwable instanceof InstantiationException) {
            return ExceptionType.INSTANTIATION_EXCEPTION;
        } else if (throwable instanceof IllegalArgumentException) {
            return ExceptionType.ILLEGAL_ARGUMENT;
        } else if (throwable instanceof NoSuchMethodException) {
            return ExceptionType.METHOD_NOT_FOUND;
        }
    }

    public enum ExceptionType {
        FIELD_NOT_FOUND,
        METHOD_NOT_FOUND,
        CONSTRUCTOR_NOT_FOUND,
        ILLEGAL_ACCESS,
        INVOCATION_EXCEPTION,
        INSTANTIATION_EXCEPTION,
        ILLEGAL_ARGUMENT,
        UNKNOWN,
    }

}