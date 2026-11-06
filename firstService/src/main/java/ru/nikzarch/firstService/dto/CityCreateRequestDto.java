package ru.nikzarch.firstService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

@Getter
@Setter
public class CityCreateRequestDto {
    @NotNull(message = "{validation.name.required}")
    @Size(min = 1, message = "{validation.name.required}")
    private String name;

    @Valid
    @NotNull(message = "{validation.coordinates.required}")
    private CoordinatesDto coordinates;

    @NotNull(message = "{validation.area.required}")
    @Positive(message = "{validation.area.positive}")
    private Long area;

    @NotNull(message = "{validation.population.required}")
    @Positive(message = "{validation.population.positive}")
    private Long population;

    private Double metersAboveSeaLevel;

    @NotNull(message = "{validation.climate.required}")
    private Climate climate;

    @NotNull(message = "{validation.government.required}")
    private Government government;

    private StandardOfLiving standardOfLiving;

    @Valid
    @NotNull(message = "{validation.governor.required}")
    private HumanDto governor;
}
