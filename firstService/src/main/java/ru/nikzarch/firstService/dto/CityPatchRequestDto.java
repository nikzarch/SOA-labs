package ru.nikzarch.firstService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

@Getter
@Setter
public class CityPatchRequestDto {
    // Preserve the original service's partial-update semantics.
    @Size(min = 1, message = "{validation.name.required}")
    private String name;

    @Valid
    private CoordinatesPatchDto coordinates;

    @Positive(message = "{validation.area.positive}")
    private Long area;

    @Positive(message = "{validation.population.positive}")
    private Long population;

    private Double metersAboveSeaLevel;
    private Climate climate;
    private Government government;
    private StandardOfLiving standardOfLiving;

    @Valid
    private HumanDto governor;
}
