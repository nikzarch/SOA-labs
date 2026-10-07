package ru.nikzarch.firstService.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class ErrorResponse {
    private final  String message;
    private final List<Map<String, String>> details;
    private final String timestamp;

    public ErrorResponse(String message, List<Map<String, String>> details) {
        this.message = message;
        this.details = details;
        this.timestamp = OffsetDateTime.now().toString();
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, new ArrayList<>());
    }

}
