package com.aafkir.tifssi.shared.application.exception;

import com.aafkir.tifssi.shared.api.error.ApiFieldError;
import java.util.List;

public class RequestValidationException extends RuntimeException {

    private final List<ApiFieldError> fieldErrors;

    public RequestValidationException(String message, List<ApiFieldError> fieldErrors) {
        super(message);
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<ApiFieldError> getFieldErrors() {
        return fieldErrors;
    }
}

