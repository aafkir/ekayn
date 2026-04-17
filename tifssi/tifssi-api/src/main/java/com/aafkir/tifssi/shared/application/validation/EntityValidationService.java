package com.aafkir.tifssi.shared.application.validation;

import com.aafkir.tifssi.shared.api.error.ApiFieldError;
import com.aafkir.tifssi.shared.application.exception.RequestValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EntityValidationService {

    private final Validator validator;

    public EntityValidationService(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (violations.isEmpty()) {
            return;
        }

        List<ApiFieldError> fieldErrors = violations.stream()
                .map(violation -> new ApiFieldError(violation.getPropertyPath().toString(), violation.getMessage()))
                .sorted(Comparator.comparing(ApiFieldError::field))
                .toList();

        throw new RequestValidationException("Validation failed.", fieldErrors);
    }
}

