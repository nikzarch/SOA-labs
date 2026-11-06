package ru.nikzarch.firstService.error;

public enum ErrorCode {
    REQUEST_INVALID("error.request.invalid"),
    REQUEST_BODY_REQUIRED("error.request.body_required"),
    OBJECT_NOT_FOUND("error.city.not_found"),
    MATCHING_CITY_NOT_FOUND("error.city.match_not_found"),
    RESOURCE_NOT_FOUND("error.resource.not_found"),
    INTERNAL_ERROR("error.server.internal"),
    PARAM_REQUIRED("error.parameter.required"),
    PARAM_POSITIVE("error.parameter.positive"),
    PARAM_INTEGER("error.parameter.integer"),
    PAGE_SIZE_RANGE("error.pagination.page_size"),
    PAGE_OVERFLOW("error.pagination.overflow"),
    FILTER_INVALID("error.filter.invalid"),
    FILTER_FORMAT("error.filter.format"),
    FILTER_UNKNOWN_FIELD("error.filter.unknown_field"),
    FILTER_OPERATOR("error.filter.operator"),
    FILTER_VALUE("error.filter.value"),
    SORT_INVALID("error.sort.invalid"),
    SORT_FORMAT("error.sort.format"),
    SORT_DIRECTION("error.sort.direction"),
    SPECIAL_UNKNOWN_PARAMETER("error.special.unknown_parameter"),
    SPECIAL_INVALID("error.special.invalid"),
    JSON_INVALID_ENUM("error.json.invalid_enum");

    private final String key;

    ErrorCode(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
