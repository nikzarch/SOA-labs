package com.truskovski.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record Coordinates (
        Integer x,

        @NotNull
        @DecimalMin(value = "-594", inclusive = false)
        Double y
) { }
