package com.truskovski.service;

import com.truskovski.model.dto.Coordinates;
import org.springframework.stereotype.Service;

@Service
public class SpecialOperationsService {

    private final Double initialX = 0d;
    private final Double initialY = 0d;

    public Double calculateDistanceToACity(Coordinates coordinates) {
        return Math.sqrt(
                Math.pow(coordinates.x() - initialX, 2) +
                        Math.pow(coordinates.y() - initialY, 2)
        );
    }
}
