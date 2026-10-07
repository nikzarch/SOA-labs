package ru.nikzarch.firstService.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class Coordinates {
    private Integer x;

    @NotNull
    @DecimalMin(value = "-594", inclusive = false)
    private Double y;

    public void setX(Integer x) { this.x = x; }

    public void setY(Double y) { this.y = y; }
}
