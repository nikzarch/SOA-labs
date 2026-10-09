package com.truskovski.model.dto;

import java.util.List;
import java.util.Map;

public record ErrorResponse (
        String message,
        List<Map<String, String>> details,
        String timestamp)
{
}
