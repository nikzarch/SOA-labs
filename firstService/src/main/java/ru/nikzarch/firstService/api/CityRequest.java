package ru.nikzarch.firstService.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CityRequest {
    @NotNull
    @jakarta.validation.constraints.Size(min = 1)
    private String name;

    @NotNull @Valid
    private Coordinates coordinates;

    @NotNull @Positive
    private Long area;

    @NotNull @Positive
    private Long population;

    private Double metersAboveSeaLevel;

    @NotNull
    private String climate;

    @NotNull
    private String government;

    private String standardOfLiving;

    @NotNull @Valid
    private Human governor;

}
