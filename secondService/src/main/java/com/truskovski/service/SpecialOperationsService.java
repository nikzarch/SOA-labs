package com.truskovski.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecialOperationsService {

    private final Double initialX = 0d;
    private final Double initialY = 0d;

    ObjectMapper objectMapper = new ObjectMapper();

    public Double calculateDistanceToACity(ResponseEntity<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return Math.sqrt(
                    Math.pow(root.path("coordinates.x").asDouble() - initialX, 2) +
                            Math.pow(root.path("coordinates.y").asDouble() - initialY, 2)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
