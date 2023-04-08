package com.github.Aseeef;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;

public class ReflectiveAseefianException extends RuntimeException {

    @Getter
    private final ExceptionType exceptionType;

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
        this.exceptionType = deduceExceptionType(cause);
    }

    public ReflectiveAseefianException(String message) {
        super(message);
        this.exceptionType = ExceptionType.UNKNOWN;
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
        } else if (throwable instanceof IllegalAccessException) {
            return ExceptionType.ILLEGAL_ACCESS;
        }
        return ExceptionType.UNKNOWN;
    }

    public enum ExceptionType {
        FIELD_NOT_FOUND,
        METHOD_NOT_FOUND,
        ENUM_NOT_FOUND,
        ILLEGAL_ACCESS,
        ILLEGAL_STATE,
        INVOCATION_EXCEPTION,
        INSTANTIATION_EXCEPTION,
        ILLEGAL_ARGUMENT,
        AMBIGUOUS_CALL,
        UNKNOWN,
    }

}
