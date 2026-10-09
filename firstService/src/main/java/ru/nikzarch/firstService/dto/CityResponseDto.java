package ru.nikzarch.firstService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

@Setter
@Getter
public class CityResponseDto  {

    private Integer id;

    @Size(min = 1)
    private String name;

    @Valid
    private CoordinatesDto coordinates;

    @Positive
    private Long area;

    @Positive
    private Long population;

    private Double metersAboveSeaLevel;

    private Climate climate;

    private Government government;

    private StandardOfLiving standardOfLiving;

    @Valid
    private HumanDto governor;

    private String creationDate;


}
