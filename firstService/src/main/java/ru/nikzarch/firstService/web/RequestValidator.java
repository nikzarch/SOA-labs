package ru.nikzarch.firstService.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.error.ErrorCode;

@ApplicationScoped
public class RequestValidator {
    @Inject
    Validator validator;

    public int validateAndParsePositiveInteger(String raw, int defaultValue, String field, boolean required) {
        if (raw == null) {
            if (required) {
                throw ApiException.badRequest(ErrorCode.PARAM_REQUIRED, field);
            }
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value < 1) {
                throw ApiException.badRequest(ErrorCode.PARAM_POSITIVE, field);
            }
            return value;
        } catch (NumberFormatException exception) {
            throw ApiException.badRequest(ErrorCode.PARAM_INTEGER, field);
        }
    }

    public void validate(Object request) {
        if (request == null) {
            throw ApiException.badRequest(ErrorCode.REQUEST_BODY_REQUIRED);
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    public void validateSpecialParam(String parameterName, String value) {
        if (!"area".equals(parameterName)) {
            return;
        }

        int area;

        try {
            area = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw ApiException.invalidField(
                    "area",
                    "validation.special.area.integer"
            );
        }

        if (area <= 0) {
            throw ApiException.invalidField(
                    "area",
                    "validation.special.area.positive"
            );
        }
    }
}
