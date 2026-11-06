package ru.nikzarch.firstService.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoordinatesPatchDto {
    private Integer x;

    @DecimalMin(value = "-594", inclusive = false, message = "{validation.coordinate_y.minimum}")
    private Double y;
}
