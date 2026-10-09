package com.truskovski.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CityRequest(
        @NotNull
        @Size(min = 1)
        String name,

        @NotNull @Valid
        Coordinates coordinates,

        @NotNull @Positive Long area,

        @NotNull @Positive Long population,

        Double metersAboveSeaLevel,

        @NotNull String climate,

        @NotNull String government,

        String standardOfLiving,

        @NotNull @Valid Human governor
) {
}
